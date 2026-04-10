package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 论坛帖子Mapper接口
 */
@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 分页查询帖子（带用户信息）
     */
    IPage<ForumPost> selectPostPage(Page<ForumPost> page, @Param("type") Integer type,
                                     @Param("keyword") String keyword, @Param("status") Integer status,
                                     @Param("sortBy") String sortBy);

    /**
     * 分页查询帖子（管理端-完整筛选）
     */
    IPage<ForumPost> selectPostPageAdmin(Page<ForumPost> page, @Param("type") Integer type,
                                          @Param("keyword") String keyword, @Param("status") Integer status,
                                          @Param("userName") String userName);

    /**
     * 查询帖子详情
     */
    ForumPost selectPostDetail(@Param("id") Long id);

    /**
     * 分页查询用户自己的帖子
     */
    IPage<ForumPost> selectMyPostPage(Page<ForumPost> page, @Param("userId") Long userId,
                                       @Param("type") Integer type, @Param("keyword") String keyword);
}
