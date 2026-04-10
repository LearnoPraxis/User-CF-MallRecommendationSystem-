package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 论坛评论DTO
 */
@Data
public class ForumCommentDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 父评论ID，0表示一级评论 */
    private Long parentId = 0L;
}
