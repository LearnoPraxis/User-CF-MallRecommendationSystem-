package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 商品留言DTO
 */
@Data
public class CommentDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "留言内容不能为空")
    private String content;

    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating = 5;
}
