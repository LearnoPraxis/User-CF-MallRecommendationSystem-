package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.UserFavorite;
import com.mall.exception.BusinessException;
import com.mall.mapper.UserFavoriteMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.RecommendService;
import com.mall.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户收藏服务实现类
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> 
        implements UserFavoriteService {

    @Autowired
    private RecommendService recommendService;

    @Override
    public void addFavorite(Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 检查是否已收藏
        long count = this.count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
        if (count > 0) {
            throw new BusinessException("已收藏该商品");
        }
        // 添加收藏
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        this.save(favorite);
        // 记录用户行为
        recommendService.updateUserProductRating(userId, productId, "favorite");
    }

    @Override
    public void removeFavorite(Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();
        this.remove(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
    }

    @Override
    public boolean isFavorite(Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        return this.count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)) > 0;
    }

    @Override
    public IPage<UserFavorite> pageUserFavorites(Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<UserFavorite> pageParam = new Page<>(page, size);
        return baseMapper.selectFavoritePage(pageParam, userId);
    }
}
