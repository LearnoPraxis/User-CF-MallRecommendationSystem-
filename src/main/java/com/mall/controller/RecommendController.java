package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.Product;
import com.mall.service.RecommendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐控制器
 */
@Api(tags = "商品推荐")
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @ApiOperation("猜你喜欢（基于协同过滤算法）")
    @GetMapping("/personalized")
    public Result<List<Product>> personalized(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = recommendService.getPersonalizedRecommendations(limit);
        return Result.success(products);
    }

    @ApiOperation("热门推荐（全局热度）")
    @GetMapping("/hot")
    public Result<List<Product>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = recommendService.getHotRecommendations(limit);
        return Result.success(products);
    }

    @ApiOperation("根据浏览记录推荐")
    @GetMapping("/history")
    public Result<List<Product>> byHistory(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = recommendService.getRecommendationsByBrowseHistory(limit);
        return Result.success(products);
    }
}
