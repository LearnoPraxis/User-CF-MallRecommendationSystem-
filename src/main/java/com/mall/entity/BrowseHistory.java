package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 浏览记录实体类
 */
@Data
@TableName("browse_history")
public class BrowseHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 浏览时间 */
    private LocalDateTime browseTime;

    /** 浏览时长（秒） */
    private Integer duration;

    /** 商品信息（非数据库字段） */
    @TableField(exist = false)
    private Product product;
}
