package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.ProductComment;
import com.mall.service.ProductCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员-留言管理控制器
 */
@Api(tags = "管理员-留言管理")
@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    @Autowired
    private ProductCommentService productCommentService;

    @ApiOperation("分页查询留言")
    @GetMapping("/list")
    public Result<PageResult<ProductComment>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer hasReply,
            @RequestParam(required = false) Integer status) {
        IPage<ProductComment> pageResult = productCommentService.pageAllCommentsAdmin(
                page, size, keyword, productId, rating, hasReply, status);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("获取留言详情")
    @GetMapping("/{id}")
    public Result<ProductComment> getById(@PathVariable Long id) {
        return Result.success(productCommentService.getById(id));
    }

    @ApiOperation("回复留言")
    @PostMapping("/reply/{id}")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String replyContent = params.get("replyContent");
        productCommentService.replyComment(id, replyContent);
        return Result.success();
    }

    @ApiOperation("更新留言状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productCommentService.updateCommentStatus(id, status);
        return Result.success();
    }

    @ApiOperation("删除留言")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productCommentService.deleteComment(id);
        return Result.success();
    }

    @ApiOperation("批量删除留言")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        productCommentService.removeByIds(ids);
        return Result.success();
    }
}
