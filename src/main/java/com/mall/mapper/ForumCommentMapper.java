package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.ForumComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 论坛评论Mapper接口
 */
@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {

    /**
     * 查询帖子评论（带用户信息）
     */
    List<ForumComment> selectCommentsByPostId(@Param("postId") Long postId);

    /**
     * 分页查询评论（管理端）
     */
    IPage<ForumComment> selectCommentPageAdmin(Page<ForumComment> page, @Param("postId") Long postId,
                                                @Param("keyword") String keyword, @Param("status") Integer status);
}
