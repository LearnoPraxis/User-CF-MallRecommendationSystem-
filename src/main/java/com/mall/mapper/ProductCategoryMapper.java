package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
