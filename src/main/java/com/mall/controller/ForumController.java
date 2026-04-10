package com.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.dto.ForumCommentDTO;
import com.mall.dto.ForumPostDTO;
import com.mall.entity.ForumComment;
import com.mall.entity.ForumPost;
import com.mall.service.ForumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛控制器
 */
@Api(tags = "论坛管理")
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @ApiOperation("分页查询帖子")
    @GetMapping("/list")
    public Result<PageResult<ForumPost>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy) {
        IPage<ForumPost> pageResult = forumService.pagePosts(page, size, type, keyword, 1, sortBy);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("获取帖子详情")
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ForumPost post = forumService.getPostDetail(id);
        List<ForumComment> comments = forumService.getPostComments(id);
        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("comments", comments);
        return Result.success(result);
    }

    @ApiOperation("发布帖子")
    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody ForumPostDTO dto) {
        forumService.createPost(dto);
        return Result.success();
    }

    @ApiOperation("更新帖子")
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody ForumPostDTO dto) {
        forumService.updatePost(dto);
        return Result.success();
    }

    @ApiOperation("删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        forumService.deletePost(id);
        return Result.success();
    }

    @ApiOperation("添加评论")
    @PostMapping("/comment")
    public Result<Void> addComment(@Validated @RequestBody ForumCommentDTO dto) {
        forumService.addComment(dto);
        return Result.success();
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        forumService.deleteComment(id);
        return Result.success();
    }

    @ApiOperation("获取当前用户的帖子列表")
    @GetMapping("/my")
    public Result<PageResult<ForumPost>> myPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        IPage<ForumPost> pageResult = forumService.pageMyPosts(page, size, type, keyword);
        return Result.success(PageResult.of(pageResult));
    }
}
