package com.mall.service;

import com.mall.entity.Product;

import java.util.List;

/**
 * 推荐服务接口
 */
public interface RecommendService {

    /**
     * 猜你喜欢（基于协同过滤算法）
     */
    List<Product> getPersonalizedRecommendations(Integer limit);

    /**
     * 热门推荐（全局热度）
     */
    List<Product> getHotRecommendations(Integer limit);

    /**
     * 根据浏览记录推荐
     */
    List<Product> getRecommendationsByBrowseHistory(Integer limit);

    /**
     * 更新用户商品评分（用于协同过滤）
     */
    void updateUserProductRating(Long userId, Long productId, String actionType);

    /**
     * 计算协同过滤推荐（可由定时任务调用）
     */
    void calculateCollaborativeFiltering();
}
