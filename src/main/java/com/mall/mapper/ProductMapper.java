package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询商品（带分类名称）
     */
    IPage<Product> selectProductPage(Page<Product> page, @Param("categoryId") Long categoryId,
                                      @Param("keyword") String keyword, @Param("status") Integer status,
                                      @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
                                      @Param("sortBy") String sortBy);

    /**
     * 根据ID列表查询商品
     */
    List<Product> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询热门商品（按销量排序）
     */
    List<Product> selectHotProducts(@Param("limit") Integer limit);

    /**
     * 查询最新商品
     */
    List<Product> selectNewProducts(@Param("limit") Integer limit);

    /**
     * 批量更新商品状态
     */
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
}
