package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单明细ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品名称（快照） */
    private String productName;

    /** 商品图片（快照） */
    private String productImage;

    /** 商品单价（快照） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal totalPrice;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
