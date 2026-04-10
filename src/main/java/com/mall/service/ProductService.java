package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.ProductDTO;
import com.mall.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品
     */
    IPage<Product> pageProducts(Integer page, Integer size, Long categoryId, String keyword, Integer status);

    /**
     * 分页查询商品（带排序和价格筛选）
     */
    IPage<Product> pageProducts(Integer page, Integer size, Long categoryId, String keyword, Integer status,
                                Double minPrice, Double maxPrice, String sortBy);

    /**
     * 分页查询用户的商品
     */
    IPage<Product> pageUserProducts(Integer page, Integer size, Long userId, String keyword, Integer status);

    /**
     * 获取商品详情
     */
    Product getProductDetail(Long id);

    /**
     * 添加商品
     */
    void addProduct(ProductDTO dto);

    /**
     * 用户发布商品
     */
    void publishProduct(ProductDTO dto);

    /**
     * 更新商品
     */
    void updateProduct(ProductDTO dto);

    /**
     * 删除商品
     */
    void deleteProduct(Long id);

    /**
     * 更新商品状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 更新商品状态（别名）
     */
    void updateProductStatus(Long id, Integer status);

    /**
     * 获取热门商品
     */
    List<Product> getHotProducts(Integer limit);

    /**
     * 获取最新商品
     */
    List<Product> getNewProducts(Integer limit);

    /**
     * 根据ID列表获取商品
     */
    List<Product> getProductsByIds(List<Long> ids);

    /**
     * 批量删除商品
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量更新商品状态
     */
    void batchUpdateStatus(List<Long> ids, Integer status);
}
