package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@TableName("order_info")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 订单状态：0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 */
    private Integer status;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 订单备注 */
    private String remark;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货时间 */
    private LocalDateTime deliverTime;

    /** 收货时间 */
    private LocalDateTime receiveTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 订单明细（非数据库字段） */
    @TableField(exist = false)
    private List<OrderItem> orderItems;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;
}
