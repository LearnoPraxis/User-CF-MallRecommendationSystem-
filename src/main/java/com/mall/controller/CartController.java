package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.CartDTO;
import com.mall.entity.Cart;
import com.mall.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@Api(tags = "购物车管理")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("获取购物车列表")
    @GetMapping("/list")
    public Result<List<Cart>> list() {
        List<Cart> carts = cartService.getUserCart();
        return Result.success(carts);
    }

    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public Result<Void> add(@Validated @RequestBody CartDTO dto) {
        cartService.addToCart(dto);
        return Result.success();
    }

    @ApiOperation("更新购物车数量")
    @PutMapping("/quantity/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity);
        return Result.success();
    }

    @ApiOperation("更新选中状态")
    @PutMapping("/selected/{id}")
    public Result<Void> updateSelected(@PathVariable Long id, @RequestParam Integer selected) {
        cartService.updateSelected(id, selected);
        return Result.success();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/selectAll")
    public Result<Void> selectAll(@RequestParam Integer selected) {
        cartService.selectAll(selected);
        return Result.success();
    }

    @ApiOperation("删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Result.success();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clearCart();
        return Result.success();
    }
}
