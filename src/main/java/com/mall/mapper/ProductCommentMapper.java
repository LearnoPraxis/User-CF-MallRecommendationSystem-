package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.ProductComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品留言Mapper接口
 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {

    /**
     * 分页查询商品留言（带用户信息）
     */
    IPage<ProductComment> selectCommentPage(Page<ProductComment> page, @Param("productId") Long productId);

    /**
     * 分页查询所有留言（管理端）
     */
    IPage<ProductComment> selectAllCommentPage(Page<ProductComment> page, @Param("keyword") String keyword);

    /**
     * 分页查询所有留言（管理端-完整筛选）
     */
    IPage<ProductComment> selectAllCommentPageAdmin(Page<ProductComment> page, 
                                                     @Param("keyword") String keyword,
                                                     @Param("productId") Long productId,
                                                     @Param("rating") Integer rating,
                                                     @Param("hasReply") Integer hasReply,
                                                     @Param("status") Integer status);
}
