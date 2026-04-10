package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.ProductDTO;
import com.mall.entity.Product;
import com.mall.exception.BusinessException;
import com.mall.mapper.ProductMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.ProductService;
import com.mall.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    @Lazy
    private RecommendService recommendService;

    @Override
    public IPage<Product> pageProducts(Integer page, Integer size, Long categoryId, String keyword, Integer status) {
        return pageProducts(page, size, categoryId, keyword, status, null, null, null);
    }

    @Override
    public IPage<Product> pageProducts(Integer page, Integer size, Long categoryId, String keyword, Integer status,
                                        Double minPrice, Double maxPrice, String sortBy) {
        Page<Product> pageParam = new Page<>(page, size);
        return baseMapper.selectProductPage(pageParam, categoryId, keyword, status, minPrice, maxPrice, sortBy);
    }

    @Override
    public IPage<Product> pageUserProducts(Integer page, Integer size, Long userId, String keyword, Integer status) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        // 增加浏览次数
        Product update = new Product();
        update.setId(id);
        update.setViewCount(product.getViewCount() + 1);
        this.updateById(update);
        // 记录用户行为（用于推荐）
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            recommendService.updateUserProductRating(userId, id, "browse");
        }
        return product;
    }

    @Override
    public void addProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        // 设置上架用户
        product.setUserId(SecurityUtil.getCurrentUserId());
        product.setSales(0);
        product.setViewCount(0);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        this.save(product);
    }

    @Override
    public void publishProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        // 设置上架用户
        product.setUserId(SecurityUtil.getCurrentUserId());
        product.setSales(0);
        product.setViewCount(0);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        this.save(product);
    }

    @Override
    public void updateProduct(ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        this.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        this.removeById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        this.updateById(product);
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        updateStatus(id, status);
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        return baseMapper.selectHotProducts(limit);
    }

    @Override
    public List<Product> getNewProducts(Integer limit) {
        return baseMapper.selectNewProducts(limit);
    }

    @Override
    public List<Product> getProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectByIds(ids);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的商品");
        }
        this.removeByIds(ids);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的商品");
        }
        baseMapper.batchUpdateStatus(ids, status);
    }
}
