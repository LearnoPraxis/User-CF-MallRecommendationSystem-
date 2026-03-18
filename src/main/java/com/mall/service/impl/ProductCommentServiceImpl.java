package com.mall.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.CommentDTO;
import com.mall.entity.ProductComment;
import com.mall.exception.BusinessException;
import com.mall.mapper.ProductCommentMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.ProductCommentService;
import com.mall.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 商品留言服务实现类
 */
@Service
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> 
        implements ProductCommentService {

    @Autowired
    private RecommendService recommendService;

    @Override
    public void addComment(CommentDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        ProductComment comment = new ProductComment();
        comment.setProductId(dto.getProductId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setRating(dto.getRating());
        comment.setStatus(1);
        this.save(comment);
        // 记录用户行为
        recommendService.updateUserProductRating(userId, dto.getProductId(), "comment");
    }

    @Override
    public IPage<ProductComment> pageProductComments(Long productId, Integer page, Integer size) {
        Page<ProductComment> pageParam = new Page<>(page, size);
        return baseMapper.selectCommentPage(pageParam, productId);
    }

    @Override
    public IPage<ProductComment> pageAllComments(Integer page, Integer size, String keyword) {
        Page<ProductComment> pageParam = new Page<>(page, size);
        return baseMapper.selectAllCommentPage(pageParam, keyword);
    }

    @Override
    public IPage<ProductComment> pageAllCommentsAdmin(Integer page, Integer size, String keyword,
                                                       Long productId, Integer rating, Integer hasReply, Integer status) {
        Page<ProductComment> pageParam = new Page<>(page, size);
        return baseMapper.selectAllCommentPageAdmin(pageParam, keyword, productId, rating, hasReply, status);
    }

    @Override
    public void replyComment(Long id, String replyContent) {
        ProductComment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException("留言不存在");
        }
        comment.setReplyContent(replyContent);
        comment.setReplyTime(LocalDateTime.now());
        comment.setReplyUserId(SecurityUtil.getCurrentUserId());
        this.updateById(comment);
    }

    @Override
    public void updateCommentStatus(Long id, Integer status) {
        ProductComment comment = new ProductComment();
        comment.setId(id);
        comment.setStatus(status);
        this.updateById(comment);
    }

    @Override
    public void deleteComment(Long id) {
        this.removeById(id);
    }
}
