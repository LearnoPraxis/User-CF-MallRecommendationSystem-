# -*- coding: utf-8 -*-
"""
配置文件
"""

# MySQL配置
MYSQL_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'mall_recommendation',
    'charset': 'utf8mb4'
}

# Redis配置
REDIS_CONFIG = {
    'host': 'localhost',
    'port': 6379,
    'db': 0,
    'decode_responses': True
}

# 推荐服务配置
RECOMMEND_CONFIG = {
    'similar_users_count': 20,      # 相似用户数量
    'recommend_count': 20,          # 推荐商品数量
    'cache_expire': 3600,           # 缓存过期时间（秒）
    'min_common_items': 2,          # 最少共同评分商品数
}

# Flask配置
FLASK_CONFIG = {
    'host': '0.0.0.0',
    'port': 5000,
    'debug': True
}
