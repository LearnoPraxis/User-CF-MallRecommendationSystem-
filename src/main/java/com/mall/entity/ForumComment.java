package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛评论实体类
 */
@Data
@TableName("forum_comment")
public class ForumComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 评论用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 父评论ID，0表示一级评论 */
    private Long parentId;

    /** 状态：0-隐藏 1-显示 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String userAvatar;

    /** 帖子标题（非数据库字段） */
    @TableField(exist = false)
    private String postTitle;
}
