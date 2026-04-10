package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品DTO
 */
@Data
public class ProductDTO {

    /** 商品ID（更新时使用） */
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

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
}
