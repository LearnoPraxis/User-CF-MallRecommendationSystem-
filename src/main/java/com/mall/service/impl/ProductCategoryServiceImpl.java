package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.ProductCategory;
import com.mall.exception.BusinessException;
import com.mall.mapper.ProductCategoryMapper;
import com.mall.service.ProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> 
        implements ProductCategoryService {

    @Override
    public List<ProductCategory> getAllCategories() {
        return this.list(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort));
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<ProductCategory> allCategories = getAllCategories();
        // 获取一级分类
        List<ProductCategory> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        // 构建树形结构
        List<Map<String, Object>> tree = new ArrayList<>();
        for (ProductCategory root : rootCategories) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", root.getId());
            node.put("name", root.getName());
            node.put("icon", root.getIcon());
            // 获取子分类
            List<Map<String, Object>> children = allCategories.stream()
                    .filter(c -> c.getParentId().equals(root.getId()))
                    .map(c -> {
                        Map<String, Object> child = new HashMap<>();
                        child.put("id", c.getId());
                        child.put("name", c.getName());
                        child.put("icon", c.getIcon());
                        return child;
                    })
                    .collect(Collectors.toList());
            node.put("children", children);
            tree.add(node);
        }
        return tree;
    }

    @Override
    public void addCategory(ProductCategory category) {
        // 检查名称是否重复
        long count = this.count(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getName, category.getName()));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        category.setStatus(1);
        this.save(category);
    }

    @Override
    public void updateCategory(ProductCategory category) {
        // 检查名称是否重复（排除自己）
        long count = this.count(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getName, category.getName())
                .ne(ProductCategory::getId, category.getId()));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        long childCount = this.count(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }
        this.removeById(id);
    }

    @Override
    public void updateCategoryStatus(Long id, Integer status) {
        ProductCategory category = new ProductCategory();
        category.setId(id);
        category.setStatus(status);
        this.updateById(category);
    }
}
