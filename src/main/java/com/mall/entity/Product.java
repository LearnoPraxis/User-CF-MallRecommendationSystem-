package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 上架用户ID */
    private Long userId;

    /** 价格 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 主图URL */
    private String mainImage;

    /** 商品图片，多个用逗号分隔 */
    private String images;

    /** 商品描述 */
    private String description;

    /** 商品详情（富文本） */
    private String detail;

    /** 状态：0-下架 1-上架 */
    private Integer status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 分类名称（非数据库字段） */
    @TableField(exist = false)
    private String categoryName;

    /** 上架用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;
}
