package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.ForumCommentDTO;
import com.mall.dto.ForumPostDTO;
import com.mall.entity.ForumComment;
import com.mall.entity.ForumPost;
import com.mall.exception.BusinessException;
import com.mall.mapper.ForumCommentMapper;
import com.mall.mapper.ForumPostMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 论坛服务实现类
 */
@Service
public class ForumServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumService {

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Override
    public IPage<ForumPost> pagePosts(Integer page, Integer size, Integer type, String keyword, Integer status) {
        return pagePosts(page, size, type, keyword, status, null);
    }

    @Override
    public IPage<ForumPost> pagePosts(Integer page, Integer size, Integer type, String keyword, Integer status, String sortBy) {
        Page<ForumPost> pageParam = new Page<>(page, size);
        return baseMapper.selectPostPage(pageParam, type, keyword, status, sortBy);
    }

    @Override
    public IPage<ForumPost> pagePostsAdmin(Integer page, Integer size, Integer type, String keyword, Integer status, String userName) {
        Page<ForumPost> pageParam = new Page<>(page, size);
        return baseMapper.selectPostPageAdmin(pageParam, type, keyword, status, userName);
    }

    @Override
    public ForumPost getPostDetail(Long id) {
        ForumPost post = baseMapper.selectPostDetail(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        // 增加浏览次数
        ForumPost update = new ForumPost();
        update.setId(id);
        update.setViewCount(post.getViewCount() + 1);
        this.updateById(update);
        return post;
    }

    @Override
    public ForumPost getPostDetailAdmin(Long id) {
        ForumPost post = baseMapper.selectPostDetail(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return post;
    }

    @Override
    public void createPost(ForumPostDTO dto) {
        ForumPost post = new ForumPost();
        post.setUserId(SecurityUtil.getCurrentUserId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setType(dto.getType());
        post.setImages(dto.getImages());
        post.setViewCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        this.save(post);
    }

    @Override
    public void updatePost(ForumPostDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        // 验证是否是自己的帖子
        ForumPost existPost = this.getById(dto.getId());
        if (existPost == null) {
            throw new BusinessException("帖子不存在");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!existPost.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能修改自己的帖子");
        }
        ForumPost post = new ForumPost();
        post.setId(dto.getId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setType(dto.getType());
        post.setImages(dto.getImages());
        this.updateById(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        // 验证是否是自己的帖子（管理员可以删除任何帖子）
        ForumPost existPost = this.getById(id);
        if (existPost == null) {
            throw new BusinessException("帖子不存在");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Boolean isAdmin = SecurityUtil.isAdmin();
        if (!isAdmin && !existPost.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己的帖子");
        }
        // 删除帖子评论
        forumCommentMapper.delete(new LambdaQueryWrapper<ForumComment>()
                .eq(ForumComment::getPostId, id));
        // 删除帖子
        this.removeById(id);
    }

    @Override
    public void updatePostStatus(Long id, Integer status) {
        ForumPost post = new ForumPost();
        post.setId(id);
        post.setStatus(status);
        this.updateById(post);
    }

    @Override
    public List<ForumComment> getPostComments(Long postId) {
        return forumCommentMapper.selectCommentsByPostId(postId);
    }

    @Override
    public void addComment(ForumCommentDTO dto) {
        // 检查帖子是否存在
        ForumPost post = this.getById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        // 添加评论
        ForumComment comment = new ForumComment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(SecurityUtil.getCurrentUserId());
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setStatus(1);
        forumCommentMapper.insert(comment);
        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        this.updateById(post);
    }

    @Override
    public void deleteComment(Long id) {
        ForumComment comment = forumCommentMapper.selectById(id);
        if (comment != null) {
            forumCommentMapper.deleteById(id);
            // 更新帖子评论数
            ForumPost post = this.getById(comment.getPostId());
            if (post != null && post.getCommentCount() > 0) {
                post.setCommentCount(post.getCommentCount() - 1);
                this.updateById(post);
            }
        }
    }

    @Override
    public IPage<ForumComment> pageCommentsAdmin(Integer page, Integer size, Long postId, String keyword, Integer status) {
        Page<ForumComment> pageParam = new Page<>(page, size);
        return forumCommentMapper.selectCommentPageAdmin(pageParam, postId, keyword, status);
    }

    @Override
    public void updateCommentStatus(Long id, Integer status) {
        ForumComment comment = new ForumComment();
        comment.setId(id);
        comment.setStatus(status);
        forumCommentMapper.updateById(comment);
    }

    @Override
    @Transactional
    public void batchDeletePosts(List<Long> ids) {
        for (Long id : ids) {
            deletePost(id);
        }
    }

    @Override
    @Transactional
    public void batchDeleteComments(List<Long> ids) {
        for (Long id : ids) {
            deleteComment(id);
        }
    }

    @Override
    public IPage<ForumPost> pageMyPosts(Integer page, Integer size, Integer type, String keyword) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<ForumPost> pageParam = new Page<>(page, size);
        return baseMapper.selectMyPostPage(pageParam, userId, type, keyword);
    }
}
