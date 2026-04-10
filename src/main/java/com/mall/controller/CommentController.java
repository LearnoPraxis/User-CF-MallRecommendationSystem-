package com.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.dto.CommentDTO;
import com.mall.entity.ProductComment;
import com.mall.service.ProductCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品留言控制器
 */
@Api(tags = "商品留言")
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private ProductCommentService productCommentService;

    @ApiOperation("分页查询商品留言")
    @GetMapping("/list/{productId}")
    public Result<PageResult<ProductComment>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProductComment> pageResult = productCommentService.pageProductComments(productId, page, size);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("添加留言")
    @PostMapping("/add")
    public Result<Void> add(@Validated @RequestBody CommentDTO dto) {
        productCommentService.addComment(dto);
        return Result.success();
    }
}
