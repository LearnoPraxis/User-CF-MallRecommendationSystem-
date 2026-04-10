package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户商品评分实体类（用于协同过滤算法）
 */
@Data
@TableName("user_product_rating")
public class UserProductRating implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 综合评分 */
    private BigDecimal rating;

    /** 浏览得分 */
    private BigDecimal browseScore;

    /** 加购得分 */
    private BigDecimal cartScore;

    /** 收藏得分 */
    private BigDecimal favoriteScore;

    /** 购买得分 */
    private BigDecimal buyScore;

    /** 评论得分 */
    private BigDecimal commentScore;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
