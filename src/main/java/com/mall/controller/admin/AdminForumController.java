package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.ForumComment;
import com.mall.entity.ForumPost;
import com.mall.service.ForumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-论坛管理控制器
 */
@Api(tags = "管理员-论坛管理")
@RestController
@RequestMapping("/api/admin/forum")
public class AdminForumController {

    @Autowired
    private ForumService forumService;

    @ApiOperation("分页查询帖子")
    @GetMapping("/list")
    public Result<PageResult<ForumPost>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userName) {
        IPage<ForumPost> pageResult = forumService.pagePostsAdmin(page, size, type, keyword, status, userName);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("获取帖子详情")
    @GetMapping("/{id}")
    public Result<ForumPost> getById(@PathVariable Long id) {
        return Result.success(forumService.getPostDetailAdmin(id));
    }

    @ApiOperation("获取帖子评论列表")
    @GetMapping("/{id}/comments")
    public Result<List<ForumComment>> getComments(@PathVariable Long id) {
        return Result.success(forumService.getPostComments(id));
    }

    @ApiOperation("分页查询所有评论")
    @GetMapping("/comment/list")
    public Result<PageResult<ForumComment>> commentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<ForumComment> pageResult = forumService.pageCommentsAdmin(page, size, postId, keyword, status);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("更新帖子状态")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        forumService.updatePostStatus(id, status);
        return Result.success();
    }

    @ApiOperation("删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        forumService.deletePost(id);
        return Result.success();
    }

    @ApiOperation("批量删除帖子")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        forumService.batchDeletePosts(ids);
        return Result.success();
    }

    @ApiOperation("更新评论状态")
    @PutMapping("/comment/status/{id}")
    public Result<Void> updateCommentStatus(@PathVariable Long id, @RequestParam Integer status) {
        forumService.updateCommentStatus(id, status);
        return Result.success();
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        forumService.deleteComment(id);
        return Result.success();
    }

    @ApiOperation("批量删除评论")
    @DeleteMapping("/comment/batch")
    public Result<Void> batchDeleteComments(@RequestBody List<Long> ids) {
        forumService.batchDeleteComments(ids);
        return Result.success();
    }
}
