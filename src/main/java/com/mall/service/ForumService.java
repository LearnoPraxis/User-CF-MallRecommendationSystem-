package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.ForumCommentDTO;
import com.mall.dto.ForumPostDTO;
import com.mall.entity.ForumComment;
import com.mall.entity.ForumPost;

import java.util.List;

/**
 * 论坛服务接口
 */
public interface ForumService extends IService<ForumPost> {

    /**
     * 分页查询帖子
     */
    IPage<ForumPost> pagePosts(Integer page, Integer size, Integer type, String keyword, Integer status);

    /**
     * 分页查询帖子（带排序）
     */
    IPage<ForumPost> pagePosts(Integer page, Integer size, Integer type, String keyword, Integer status, String sortBy);

    /**
     * 分页查询帖子（管理端-完整筛选）
     */
    IPage<ForumPost> pagePostsAdmin(Integer page, Integer size, Integer type, String keyword, Integer status, String userName);

    /**
     * 获取帖子详情
     */
    ForumPost getPostDetail(Long id);

    /**
     * 获取帖子详情（管理端-不增加浏览量）
     */
    ForumPost getPostDetailAdmin(Long id);

    /**
     * 发布帖子
     */
    void createPost(ForumPostDTO dto);

    /**
     * 更新帖子
     */
    void updatePost(ForumPostDTO dto);

    /**
     * 删除帖子
     */
    void deletePost(Long id);

    /**
     * 更新帖子状态
     */
    void updatePostStatus(Long id, Integer status);

    /**
     * 获取帖子评论
     */
    List<ForumComment> getPostComments(Long postId);

    /**
     * 添加评论
     */
    void addComment(ForumCommentDTO dto);

    /**
     * 删除评论
     */
    void deleteComment(Long id);

    /**
     * 分页查询评论（管理端）
     */
    IPage<ForumComment> pageCommentsAdmin(Integer page, Integer size, Long postId, String keyword, Integer status);

    /**
     * 更新评论状态
     */
    void updateCommentStatus(Long id, Integer status);

    /**
     * 批量删除帖子
     */
    void batchDeletePosts(List<Long> ids);

    /**
     * 批量删除评论
     */
    void batchDeleteComments(List<Long> ids);

    /**
     * 分页查询当前用户的帖子
     */
    IPage<ForumPost> pageMyPosts(Integer page, Integer size, Integer type, String keyword);
}
