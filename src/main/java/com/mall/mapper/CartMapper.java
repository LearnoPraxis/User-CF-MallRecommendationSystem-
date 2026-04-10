package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 查询用户购物车（带商品信息）
     */
    List<Cart> selectCartWithProduct(@Param("userId") Long userId);

    /**
     * 查询用户选中的购物车项
     */
    List<Cart> selectSelectedCarts(@Param("userId") Long userId);

    /**
     * 管理端分页查询购物车（带用户和商品信息）
     */
    IPage<Cart> selectCartPageAdmin(Page<Cart> page, @Param("userId") Long userId, 
                                     @Param("userName") String userName, 
                                     @Param("productName") String productName);

    /**
     * 插入或更新购物车（解决并发问题）
     */
    void insertOrUpdate(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") Integer quantity);
}
