package com.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.BrowseHistory;
import com.mall.service.BrowseHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 浏览记录控制器
 */
@Api(tags = "浏览记录")
@RestController
@RequestMapping("/api/history")
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @ApiOperation("分页查询浏览记录")
    @GetMapping("/list")
    public Result<PageResult<BrowseHistory>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<BrowseHistory> pageResult = browseHistoryService.pageUserHistory(page, size);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("删除浏览记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        browseHistoryService.deleteHistory(id);
        return Result.success();
    }

    @ApiOperation("清空浏览记录")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        browseHistoryService.clearHistory();
        return Result.success();
    }
}
