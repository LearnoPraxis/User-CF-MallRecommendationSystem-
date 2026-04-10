package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.CartDTO;
import com.mall.entity.Cart;
import com.mall.entity.Product;
import com.mall.exception.BusinessException;
import com.mall.mapper.CartMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.CartService;
import com.mall.service.ProductService;
import com.mall.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendService recommendService;

    @Override
    public List<Cart> getUserCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        return baseMapper.selectCartWithProduct(userId);
    }

    @Override
    public void addToCart(CartDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 检查商品是否存在
        Product product = productService.getById(dto.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        // 检查库存
        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException("库存不足");
        }
        // 使用 INSERT ON DUPLICATE KEY UPDATE 避免并发问题
        baseMapper.insertOrUpdate(userId, dto.getProductId(), dto.getQuantity());
        // 记录用户行为
        recommendService.updateUserProductRating(userId, dto.getProductId(), "cart");
    }

    @Override
    public void updateQuantity(Long cartId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null) {
            throw new BusinessException("购物车项不存在");
        }
        // 检查库存
        Product product = productService.getById(cart.getProductId());
        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        cart.setQuantity(quantity);
        this.updateById(cart);
    }

    @Override
    public void updateSelected(Long cartId, Integer selected) {
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setSelected(selected);
        this.updateById(cart);
    }

    @Override
    public void selectAll(Integer selected) {
        Long userId = SecurityUtil.getCurrentUserId();
        this.update(new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected));
    }

    @Override
    public void deleteCartItem(Long cartId) {
        this.removeById(cartId);
    }

    @Override
    public void clearCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        this.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }

    @Override
    public List<Cart> getSelectedCarts() {
        Long userId = SecurityUtil.getCurrentUserId();
        return baseMapper.selectSelectedCarts(userId);
    }

    @Override
    public void clearUserCart(Long userId) {
        this.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }
}
