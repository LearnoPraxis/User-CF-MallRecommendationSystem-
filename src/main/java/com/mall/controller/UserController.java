package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.PasswordDTO;
import com.mall.dto.UserUpdateDTO;
import com.mall.entity.User;
import com.mall.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo() {
        User user = userService.getCurrentUserInfo();
        return Result.success(user);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success();
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Validated @RequestBody PasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }
}
