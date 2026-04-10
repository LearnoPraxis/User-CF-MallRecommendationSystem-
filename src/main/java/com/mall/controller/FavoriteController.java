package com.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.UserFavorite;
import com.mall.service.UserFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@Api(tags = "收藏管理")
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    @ApiOperation("分页查询收藏列表")
    @GetMapping("/list")
    public Result<PageResult<UserFavorite>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<UserFavorite> pageResult = userFavoriteService.pageUserFavorites(page, size);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("添加收藏")
    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId) {
        userFavoriteService.addFavorite(productId);
        return Result.success();
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        userFavoriteService.removeFavorite(productId);
        return Result.success();
    }

    @ApiOperation("检查是否已收藏")
    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@PathVariable Long productId) {
        boolean isFavorite = userFavoriteService.isFavorite(productId);
        return Result.success(isFavorite);
    }
}
