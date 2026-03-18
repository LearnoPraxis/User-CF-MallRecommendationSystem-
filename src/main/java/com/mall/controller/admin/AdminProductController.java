package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.dto.ProductDTO;
import com.mall.entity.Product;
import com.mall.entity.ProductCategory;
import com.mall.service.ProductCategoryService;
import com.mall.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-商品管理控制器
 */
@Api(tags = "管理员-商品管理")
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService categoryService;

    @ApiOperation("分页查询商品")
    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<Product> pageResult = productService.pageProducts(page, size, categoryId, keyword, status);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("添加商品")
    @PostMapping("/add")
    public Result<Void> add(@Validated @RequestBody ProductDTO dto) {
        productService.addProduct(dto);
        return Result.success();
    }

    @ApiOperation("更新商品")
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody ProductDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @ApiOperation("更新商品状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("批量删除商品")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        productService.batchDelete(ids);
        return Result.success();
    }

    @ApiOperation("批量上架商品")
    @PutMapping("/batch/on")
    public Result<Void> batchOnShelf(@RequestBody List<Long> ids) {
        productService.batchUpdateStatus(ids, 1);
        return Result.success();
    }

    @ApiOperation("批量下架商品")
    @PutMapping("/batch/off")
    public Result<Void> batchOffShelf(@RequestBody List<Long> ids) {
        productService.batchUpdateStatus(ids, 0);
        return Result.success();
    }

    // ========== 分类管理 ==========

    @ApiOperation("获取所有分类（含禁用）")
    @GetMapping("/category/list")
    public Result<List<ProductCategory>> categoryList() {
        List<ProductCategory> list = categoryService.list();
        return Result.success(list);
    }

    @ApiOperation("添加分类")
    @PostMapping("/category/add")
    public Result<Void> addCategory(@RequestBody ProductCategory category) {
        categoryService.addCategory(category);
        return Result.success();
    }

    @ApiOperation("更新分类")
    @PutMapping("/category/update")
    public Result<Void> updateCategory(@RequestBody ProductCategory category) {
        categoryService.updateCategory(category);
        return Result.success();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @ApiOperation("更新分类状态")
    @PutMapping("/category/status/{id}")
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateCategoryStatus(id, status);
        return Result.success();
    }
}
