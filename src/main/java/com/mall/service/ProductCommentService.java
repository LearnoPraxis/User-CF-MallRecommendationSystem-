package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.CommentDTO;
import com.mall.entity.ProductComment;

/**
 * 商品留言服务接口
 */
public interface ProductCommentService extends IService<ProductComment> {

    /**
     * 添加留言
     */
    void addComment(CommentDTO dto);

    /**
     * 分页查询商品留言
     */
    IPage<ProductComment> pageProductComments(Long productId, Integer page, Integer size);

    /**
     * 分页查询所有留言（管理端）
     */
    IPage<ProductComment> pageAllComments(Integer page, Integer size, String keyword);

    /**
     * 分页查询所有留言（管理端-完整筛选）
     */
    IPage<ProductComment> pageAllCommentsAdmin(Integer page, Integer size, String keyword, 
                                                Long productId, Integer rating, Integer hasReply, Integer status);

    /**
     * 回复留言
     */
    void replyComment(Long id, String replyContent);

    /**
     * 更新留言状态
     */
    void updateCommentStatus(Long id, Integer status);

    /**
     * 删除留言
     */
    void deleteComment(Long id);
}
