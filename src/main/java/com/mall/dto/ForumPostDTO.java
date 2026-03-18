package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 论坛帖子DTO
 */
@Data
public class ForumPostDTO {

    /** 帖子ID（更新时使用） */
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "类型不能为空")
    private Integer type;

    /** 图片，多个用逗号分隔 */
    private String images;
}
