package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 */
@Data
@TableName("user_favorite")
public class UserFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收藏ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 商品信息（非数据库字段） */
    @TableField(exist = false)
    private Product product;
}
