package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 */
@Data
@TableName("forum_post")
public class ForumPost implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布用户ID */
    private Long userId;

    /** 帖子标题 */
    private String title;

    /** 帖子内容 */
    private String content;

    /** 类型：0-普通讨论 1-志愿者招募 2-产品分享 */
    private Integer type;

    /** 图片，多个用逗号分隔 */
    private String images;

    /** 浏览次数 */
    private Integer viewCount;

    /** 评论数 */
    private Integer commentCount;

    /** 状态：0-隐藏 1-显示 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String userAvatar;
}
