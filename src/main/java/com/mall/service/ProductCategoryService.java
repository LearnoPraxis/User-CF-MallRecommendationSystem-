package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.ProductCategory;

import java.util.List;
import java.util.Map;

/**
 * 商品分类服务接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 获取所有分类
     */
    List<ProductCategory> getAllCategories();

    /**
     * 获取分类树
     */
    List<Map<String, Object>> getCategoryTree();

    /**
     * 添加分类
     */
    void addCategory(ProductCategory category);

    /**
     * 更新分类
     */
    void updateCategory(ProductCategory category);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);

    /**
     * 更新分类状态
     */
    void updateCategoryStatus(Long id, Integer status);
}
