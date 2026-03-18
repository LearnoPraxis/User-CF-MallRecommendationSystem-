# -*- coding: utf-8 -*-
"""
推荐服务 Flask 应用
提供协同过滤推荐 API
"""

from flask import Flask, jsonify, request
from flask_cors import CORS
import pymysql
import redis
import json
import threading
import time
from datetime import datetime

from config import MYSQL_CONFIG, REDIS_CONFIG, RECOMMEND_CONFIG, FLASK_CONFIG
from collaborative_filtering import CollaborativeFiltering
import pandas as pd

app = Flask(__name__)
CORS(app)

# 全局变量
cf_model = None
redis_client = None
model_lock = threading.Lock()


def log(message):
    """打印日志"""
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {message}")


def get_db_connection():
    """获取数据库连接"""
    return pymysql.connect(**MYSQL_CONFIG)


def init_redis():
    """初始化Redis连接"""
    global redis_client
    try:
        redis_client = redis.Redis(**REDIS_CONFIG)
        redis_client.ping()
        log("Redis连接成功")
    except Exception as e:
        log(f"Redis连接失败: {e}，将不使用缓存")
        redis_client = None


def load_ratings_from_db():
    """从数据库加载评分数据"""
    log("从数据库加载评分数据...")
    
    conn = get_db_connection()
    try:
        # 查询用户商品评分表
        sql = """
            SELECT user_id, product_id, rating 
            FROM user_product_rating 
            WHERE rating > 0
        """
        df = pd.read_sql(sql, conn)
        log(f"加载评分数据: {len(df)} 条")
        return df
    finally:
        conn.close()


def train_model():
    """训练推荐模型"""
    global cf_model
    
    log("========== 开始训练推荐模型 ==========")
    
    with model_lock:
        # 加载评分数据
        ratings_df = load_ratings_from_db()
        
        if ratings_df.empty:
            log("警告: 没有评分数据，模型训练跳过")
            return False
        
        # 创建并训练模型
        cf_model = CollaborativeFiltering(
            similar_users_count=RECOMMEND_CONFIG['similar_users_count'],
            min_common_items=RECOMMEND_CONFIG['min_common_items']
        )
        cf_model.fit(ratings_df)
        
        log("========== 模型训练完成 ==========")
        return True


def get_cache_key(prefix, user_id):
    """生成缓存键"""
    return f"py_recommend:{prefix}:{user_id}"


def get_from_cache(key):
    """从缓存获取数据"""
    if redis_client is None:
        return None
    try:
        data = redis_client.get(key)
        if data:
            return json.loads(data)
    except Exception as e:
        log(f"缓存读取失败: {e}")
    return None


def set_to_cache(key, data, expire=None):
    """设置缓存"""
    if redis_client is None:
        return
    try:
        expire = expire or RECOMMEND_CONFIG['cache_expire']
        redis_client.setex(key, expire, json.dumps(data))
    except Exception as e:
        log(f"缓存写入失败: {e}")


def get_product_details(product_ids):
    """获取商品详情"""
    if not product_ids:
        return []
    
    conn = get_db_connection()
    try:
        placeholders = ','.join(['%s'] * len(product_ids))
        sql = f"""
            SELECT id, name, main_image as mainImage, price, sales, stock, status
            FROM product 
            WHERE id IN ({placeholders}) AND status = 1
        """
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        cursor.execute(sql, product_ids)
        products = cursor.fetchall()
        
        # 按原始顺序排序
        product_map = {p['id']: p for p in products}
        result = []
        for pid in product_ids:
            if pid in product_map:
                p = product_map[pid]
                # 转换Decimal为float
                p['price'] = float(p['price']) if p['price'] else 0
                result.append(p)
        
        return result
    finally:
        conn.close()


def get_hot_products(limit=10):
    """获取热门商品"""
    conn = get_db_connection()
    try:
        sql = """
            SELECT id, name, main_image as mainImage, price, sales, stock, status
            FROM product 
            WHERE status = 1 
            ORDER BY sales DESC, view_count DESC 
            LIMIT %s
        """
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        cursor.execute(sql, (limit,))
        products = cursor.fetchall()
        
        for p in products:
            p['price'] = float(p['price']) if p['price'] else 0
        
        return products
    finally:
        conn.close()


@app.route('/api/recommend/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'model_loaded': cf_model is not None,
        'redis_connected': redis_client is not None
    })


@app.route('/api/recommend/personalized', methods=['GET'])
def personalized_recommend():
    """
    个性化推荐（基于协同过滤算法）
    
    Query参数:
        user_id: 用户ID
        limit: 推荐数量，默认10
    """
    user_id = request.args.get('user_id', type=int)
    limit = request.args.get('limit', 10, type=int)
    
    log(f"收到个性化推荐请求: user_id={user_id}, limit={limit}")
    
    if not user_id:
        log("用户未登录，返回热门推荐")
        products = get_hot_products(limit)
        return jsonify({
            'code': 200,
            'data': products,
            'algorithm': 'hot',
            'message': '热门推荐（用户未登录）'
        })
    
    # 尝试从缓存获取
    cache_key = get_cache_key('personalized', user_id)
    cached = get_from_cache(cache_key)
    if cached:
        log(f"从缓存获取推荐结果: {len(cached)} 个商品")
        products = get_product_details(cached[:limit])
        return jsonify({
            'code': 200,
            'data': products,
            'algorithm': 'collaborative_filtering',
            'message': '协同过滤推荐（缓存）'
        })
    
    # 使用协同过滤算法
    if cf_model is None:
        log("模型未加载，返回热门推荐")
        products = get_hot_products(limit)
        return jsonify({
            'code': 200,
            'data': products,
            'algorithm': 'hot',
            'message': '热门推荐（模型未加载）'
        })
    
    # 生成推荐
    recommendations = cf_model.recommend(user_id, n_recommendations=limit * 2)
    
    if not recommendations:
        log(f"用户 {user_id} 无推荐结果，返回热门推荐")
        products = get_hot_products(limit)
        return jsonify({
            'code': 200,
            'data': products,
            'algorithm': 'hot',
            'message': '热门推荐（无个性化数据）'
        })
    
    # 获取推荐商品ID
    product_ids = [int(r[0]) for r in recommendations]
    
    # 缓存结果
    set_to_cache(cache_key, product_ids)
    
    # 获取商品详情
    products = get_product_details(product_ids[:limit])
    
    log(f"协同过滤推荐成功: {len(products)} 个商品")
    
    return jsonify({
        'code': 200,
        'data': products,
        'algorithm': 'collaborative_filtering',
        'message': '基于协同过滤算法的个性化推荐'
    })


@app.route('/api/recommend/similar_users', methods=['GET'])
def similar_users():
    """获取相似用户"""
    user_id = request.args.get('user_id', type=int)
    limit = request.args.get('limit', 10, type=int)
    
    if not user_id or cf_model is None:
        return jsonify({'code': 200, 'data': []})
    
    users = cf_model.get_similar_users(user_id, limit)
    
    return jsonify({
        'code': 200,
        'data': [{'userId': u[0], 'similarity': round(u[1], 4)} for u in users]
    })


@app.route('/api/recommend/train', methods=['POST'])
def trigger_train():
    """触发模型训练"""
    log("收到模型训练请求")
    
    success = train_model()
    
    return jsonify({
        'code': 200 if success else 500,
        'message': '模型训练成功' if success else '模型训练失败'
    })


@app.route('/api/recommend/clear_cache', methods=['POST'])
def clear_cache():
    """清除用户推荐缓存"""
    user_id = request.args.get('user_id', type=int)
    
    if redis_client and user_id:
        cache_key = get_cache_key('personalized', user_id)
        redis_client.delete(cache_key)
        log(f"清除用户 {user_id} 的推荐缓存")
    
    return jsonify({'code': 200, 'message': '缓存已清除'})


def background_train():
    """后台定时训练模型"""
    while True:
        time.sleep(3600)  # 每小时训练一次
        log("定时任务: 重新训练模型")
        train_model()


if __name__ == '__main__':
    log("========== 推荐服务启动 ==========")
    
    # 初始化Redis
    init_redis()
    
    # 初始训练模型
    train_model()
    
    # 启动后台训练线程
    train_thread = threading.Thread(target=background_train, daemon=True)
    train_thread.start()
    log("后台训练线程已启动")
    
    # 启动Flask应用
    log(f"Flask服务启动: http://{FLASK_CONFIG['host']}:{FLASK_CONFIG['port']}")
    app.run(**FLASK_CONFIG)
