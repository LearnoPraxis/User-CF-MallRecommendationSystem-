package com.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.dto.ProductDTO;
import com.mall.entity.Product;
import com.mall.service.BrowseHistoryService;
import com.mall.service.ProductService;
import com.mall.service.UserFavoriteService;
import com.mall.security.SecurityUtil;
import com.mall.exception.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserFavoriteService userFavoriteService;

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @ApiOperation("分页查询商品")
    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy) {
        IPage<Product> pageResult = productService.pageProducts(page, size, categoryId, keyword, 1, minPrice, maxPrice, sortBy);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        // 记录浏览
        browseHistoryService.recordBrowse(id);
        // 检查是否已收藏
        boolean isFavorite = userFavoriteService.isFavorite(id);
        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("isFavorite", isFavorite);
        return Result.success(result);
    }

    @ApiOperation("获取热门商品")
    @GetMapping("/hot")
    public Result<List<Product>> hotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        return Result.success(products);
    }

    @ApiOperation("获取最新商品")
    @GetMapping("/new")
    public Result<List<Product>> newProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getNewProducts(limit);
        return Result.success(products);
    }

    // ==================== 用户商品管理接口 ====================

    @ApiOperation("获取我的商品列表")
    @GetMapping("/my")
    public Result<PageResult<Product>> myProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtil.getCurrentUserId();
        IPage<Product> pageResult = productService.pageUserProducts(page, size, userId, keyword, status);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("用户发布商品")
    @PostMapping("/publish")
    public Result<Void> publishProduct(@Validated @RequestBody ProductDTO dto) {
        productService.publishProduct(dto);
        return Result.success();
    }

    @ApiOperation("用户更新自己的商品")
    @PutMapping("/my/update")
    public Result<Void> updateMyProduct(@Validated @RequestBody ProductDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        Product product = productService.getById(dto.getId());
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }
        productService.updateProduct(dto);
        return Result.success();
    }

    @ApiOperation("用户删除自己的商品")
    @DeleteMapping("/my/{id}")
    public Result<Void> deleteMyProduct(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Product product = productService.getById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }
        productService.removeById(id);
        return Result.success();
    }

    @ApiOperation("用户上下架自己的商品")
    @PutMapping("/my/{id}/status")
    public Result<Void> updateMyProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long userId = SecurityUtil.getCurrentUserId();
        Product product = productService.getById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }
        productService.updateProductStatus(id, status);
        return Result.success();
    }

    @ApiOperation("用户更新商品（旧接口保留）")
    @PutMapping("/update")
    public Result<Void> updateProduct(@Validated @RequestBody ProductDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }
}
