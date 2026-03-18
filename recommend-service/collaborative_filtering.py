# -*- coding: utf-8 -*-
"""
协同过滤推荐算法实现
基于用户的协同过滤（User-Based Collaborative Filtering）
"""

import numpy as np
import pandas as pd
from scipy.sparse import csr_matrix
from sklearn.metrics.pairwise import cosine_similarity
from datetime import datetime


class CollaborativeFiltering:
    """
    基于用户的协同过滤推荐算法
    
    算法原理：
    1. 构建用户-商品评分矩阵
    2. 计算用户之间的相似度（余弦相似度）
    3. 找到目标用户的相似用户
    4. 根据相似用户的评分预测目标用户对未交互商品的评分
    5. 返回预测评分最高的商品作为推荐结果
    """
    
    def __init__(self, similar_users_count=20, min_common_items=2):
        """
        初始化协同过滤算法
        
        Args:
            similar_users_count: 相似用户数量
            min_common_items: 最少共同评分商品数
        """
        self.similar_users_count = similar_users_count
        self.min_common_items = min_common_items
        self.user_item_matrix = None
        self.user_similarity_matrix = None
        self.user_ids = None
        self.product_ids = None
        self.user_id_to_idx = None
        self.product_id_to_idx = None
        self.idx_to_product_id = None
        
        print(f"[{self._now()}] 协同过滤算法初始化完成")
        print(f"[{self._now()}] 参数: similar_users_count={similar_users_count}, min_common_items={min_common_items}")
    
    def _now(self):
        """获取当前时间字符串"""
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    def fit(self, ratings_df):
        """
        训练模型：构建用户-商品评分矩阵并计算用户相似度
        
        Args:
            ratings_df: 评分数据DataFrame，包含 user_id, product_id, rating 列
        """
        print(f"\n[{self._now()}] ========== 开始训练协同过滤模型 ==========")
        print(f"[{self._now()}] 评分数据量: {len(ratings_df)} 条")
        
        if ratings_df.empty:
            print(f"[{self._now()}] 警告: 评分数据为空，无法训练模型")
            return
        
        # 获取唯一用户和商品
        self.user_ids = ratings_df['user_id'].unique()
        self.product_ids = ratings_df['product_id'].unique()
        
        print(f"[{self._now()}] 用户数量: {len(self.user_ids)}")
        print(f"[{self._now()}] 商品数量: {len(self.product_ids)}")
        
        # 创建ID到索引的映射
        self.user_id_to_idx = {uid: idx for idx, uid in enumerate(self.user_ids)}
        self.product_id_to_idx = {pid: idx for idx, pid in enumerate(self.product_ids)}
        self.idx_to_product_id = {idx: pid for pid, idx in self.product_id_to_idx.items()}
        
        # 构建用户-商品评分矩阵（稀疏矩阵）
        print(f"[{self._now()}] 构建用户-商品评分矩阵...")
        row_indices = [self.user_id_to_idx[uid] for uid in ratings_df['user_id']]
        col_indices = [self.product_id_to_idx[pid] for pid in ratings_df['product_id']]
        values = ratings_df['rating'].values
        
        self.user_item_matrix = csr_matrix(
            (values, (row_indices, col_indices)),
            shape=(len(self.user_ids), len(self.product_ids))
        )
        
        print(f"[{self._now()}] 评分矩阵形状: {self.user_item_matrix.shape}")
        print(f"[{self._now()}] 评分矩阵稀疏度: {1 - self.user_item_matrix.nnz / (self.user_item_matrix.shape[0] * self.user_item_matrix.shape[1]):.4f}")
        
        # 计算用户相似度矩阵（余弦相似度）
        print(f"[{self._now()}] 计算用户相似度矩阵（余弦相似度）...")
        self.user_similarity_matrix = cosine_similarity(self.user_item_matrix)
        
        print(f"[{self._now()}] 用户相似度矩阵形状: {self.user_similarity_matrix.shape}")
        print(f"[{self._now()}] ========== 模型训练完成 ==========\n")
    
    def recommend(self, user_id, n_recommendations=10, exclude_interacted=True):
        """
        为指定用户生成推荐
        
        Args:
            user_id: 用户ID
            n_recommendations: 推荐数量
            exclude_interacted: 是否排除用户已交互的商品
            
        Returns:
            推荐商品ID列表及其预测评分
        """
        print(f"\n[{self._now()}] ========== 为用户 {user_id} 生成推荐 ==========")
        
        if self.user_item_matrix is None:
            print(f"[{self._now()}] 错误: 模型未训练")
            return []
        
        if user_id not in self.user_id_to_idx:
            print(f"[{self._now()}] 用户 {user_id} 不在训练数据中，返回空推荐")
            return []
        
        user_idx = self.user_id_to_idx[user_id]
        print(f"[{self._now()}] 用户索引: {user_idx}")
        
        # 获取用户相似度
        user_similarities = self.user_similarity_matrix[user_idx]
        print(f"[{self._now()}] 用户相似度范围: [{user_similarities.min():.4f}, {user_similarities.max():.4f}]")
        
        # 找到最相似的用户（排除自己）
        similar_user_indices = np.argsort(user_similarities)[::-1][1:self.similar_users_count + 1]
        similar_user_scores = user_similarities[similar_user_indices]
        
        print(f"[{self._now()}] 找到 {len(similar_user_indices)} 个相似用户")
        print(f"[{self._now()}] 相似用户相似度: {similar_user_scores[:5]}...")
        
        # 获取用户已交互的商品
        user_interacted = set(self.user_item_matrix[user_idx].nonzero()[1])
        print(f"[{self._now()}] 用户已交互商品数: {len(user_interacted)}")
        
        # 计算预测评分
        print(f"[{self._now()}] 计算商品预测评分...")
        product_scores = {}
        
        for sim_idx, similarity in zip(similar_user_indices, similar_user_scores):
            if similarity <= 0:
                continue
            
            # 获取相似用户评分过的商品
            sim_user_ratings = self.user_item_matrix[sim_idx].toarray().flatten()
            
            for product_idx, rating in enumerate(sim_user_ratings):
                if rating > 0:
                    if exclude_interacted and product_idx in user_interacted:
                        continue
                    
                    if product_idx not in product_scores:
                        product_scores[product_idx] = {'score': 0, 'weight': 0}
                    
                    product_scores[product_idx]['score'] += similarity * rating
                    product_scores[product_idx]['weight'] += similarity
        
        # 计算加权平均评分
        final_scores = []
        for product_idx, data in product_scores.items():
            if data['weight'] > 0:
                predicted_rating = data['score'] / data['weight']
                product_id = self.idx_to_product_id[product_idx]
                final_scores.append((product_id, predicted_rating))
        
        # 按预测评分排序
        final_scores.sort(key=lambda x: x[1], reverse=True)
        recommendations = final_scores[:n_recommendations]
        
        print(f"[{self._now()}] 生成 {len(recommendations)} 个推荐")
        if recommendations:
            print(f"[{self._now()}] Top 5 推荐: {recommendations[:5]}")
        print(f"[{self._now()}] ========== 推荐生成完成 ==========\n")
        
        return recommendations
    
    def get_similar_users(self, user_id, n_users=10):
        """
        获取与指定用户最相似的用户
        
        Args:
            user_id: 用户ID
            n_users: 返回的相似用户数量
            
        Returns:
            相似用户列表 [(user_id, similarity), ...]
        """
        if self.user_similarity_matrix is None or user_id not in self.user_id_to_idx:
            return []
        
        user_idx = self.user_id_to_idx[user_id]
        similarities = self.user_similarity_matrix[user_idx]
        
        similar_indices = np.argsort(similarities)[::-1][1:n_users + 1]
        
        result = []
        for idx in similar_indices:
            sim_user_id = self.user_ids[idx]
            similarity = similarities[idx]
            result.append((int(sim_user_id), float(similarity)))
        
        return result
