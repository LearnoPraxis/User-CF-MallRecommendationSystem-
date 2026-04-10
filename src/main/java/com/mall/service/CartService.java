package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.CartDTO;
import com.mall.entity.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    /**
     * 获取用户购物车
     */
    List<Cart> getUserCart();

    /**
     * 添加商品到购物车
     */
    void addToCart(CartDTO dto);

    /**
     * 更新购物车数量
     */
    void updateQuantity(Long cartId, Integer quantity);

    /**
     * 更新购物车选中状态
     */
    void updateSelected(Long cartId, Integer selected);

    /**
     * 全选/取消全选
     */
    void selectAll(Integer selected);

    /**
     * 删除购物车项
     */
    void deleteCartItem(Long cartId);

    /**
     * 清空购物车
     */
    void clearCart();

    /**
     * 获取选中的购物车项
     */
    List<Cart> getSelectedCarts();

    /**
     * 清空指定用户购物车（管理端）
     */
    void clearUserCart(Long userId);
}
