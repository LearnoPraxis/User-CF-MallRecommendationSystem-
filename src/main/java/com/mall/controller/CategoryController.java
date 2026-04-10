package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.ProductCategory;
import com.mall.service.ProductCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品分类控制器
 */
@Api(tags = "商品分类")
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private ProductCategoryService categoryService;

    @ApiOperation("获取所有分类")
    @GetMapping("/list")
    public Result<List<ProductCategory>> list() {
        List<ProductCategory> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        List<Map<String, Object>> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }
}
