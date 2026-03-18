package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.UserProductRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户商品评分Mapper接口（协同过滤算法）
 */
@Mapper
public interface UserProductRatingMapper extends BaseMapper<UserProductRating> {

    /**
     * 查询所有用户评分数据（用于协同过滤计算）
     */
    List<UserProductRating> selectAllRatings();

    /**
     * 查询用户的评分数据
     */
    List<UserProductRating> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询对某商品有评分的用户
     */
    List<UserProductRating> selectByProductId(@Param("productId") Long productId);

    /**
     * 插入或更新用户行为评分（解决并发问题）
     */
    void insertOrUpdateRating(@Param("userId") Long userId, 
                               @Param("productId") Long productId,
                               @Param("actionType") String actionType,
                               @Param("weight") BigDecimal weight);
}
