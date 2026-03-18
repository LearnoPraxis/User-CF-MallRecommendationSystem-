package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.Cart;
import com.mall.mapper.CartMapper;
import com.mall.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-购物车管理控制器
 */
@Api(tags = "管理员-购物车管理")
@RestController
@RequestMapping("/api/admin/cart")
public class AdminCartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @ApiOperation("分页查询购物车")
    @GetMapping("/list")
    public Result<PageResult<Cart>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String productName) {
        Page<Cart> pageParam = new Page<>(page, size);
        IPage<Cart> pageResult = cartMapper.selectCartPageAdmin(pageParam, userId, userName, productName);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.removeById(id);
        return Result.success();
    }

    @ApiOperation("批量删除购物车项")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        cartService.removeByIds(ids);
        return Result.success();
    }

    @ApiOperation("清空用户购物车")
    @DeleteMapping("/clear/{userId}")
    public Result<Void> clearUserCart(@PathVariable Long userId) {
        cartService.clearUserCart(userId);
        return Result.success();
    }
}
