package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品留言/评论实体类
 */
@Data
@TableName("product_comment")
public class ProductComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 留言ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 留言内容 */
    private String content;

    /** 评分：1-5星 */
    private Integer rating;

    /** 管理员回复内容 */
    private String replyContent;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 回复人ID */
    private Long replyUserId;

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

    /** 商品名称（非数据库字段） */
    @TableField(exist = false)
    private String productName;

    /** 商品图片（非数据库字段） */
    @TableField(exist = false)
    private String productImage;

    /** 回复人名称（非数据库字段） */
    @TableField(exist = false)
    private String replyUserName;
}
