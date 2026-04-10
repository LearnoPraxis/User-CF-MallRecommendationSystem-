package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
@TableName("product_category")
public class ProductCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID，0表示一级分类 */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 分类图标 */
    private String icon;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
