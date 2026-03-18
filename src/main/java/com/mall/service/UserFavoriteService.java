package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.UserFavorite;

/**
 * 用户收藏服务接口
 */
public interface UserFavoriteService extends IService<UserFavorite> {

    /**
     * 添加收藏
     */
    void addFavorite(Long productId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long productId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Long productId);

    /**
     * 分页查询用户收藏
     */
    IPage<UserFavorite> pageUserFavorites(Integer page, Integer size);
}
