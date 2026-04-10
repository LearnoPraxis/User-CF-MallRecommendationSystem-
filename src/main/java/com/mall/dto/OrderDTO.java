package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 订单DTO
 */
@Data
public class OrderDTO {

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    /** 订单备注 */
    private String remark;
}
