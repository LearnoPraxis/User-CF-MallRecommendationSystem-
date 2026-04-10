package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.LoginDTO;
import com.mall.dto.PasswordDTO;
import com.mall.dto.RegisterDTO;
import com.mall.dto.UserUpdateDTO;
import com.mall.entity.User;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * 用户注册
     */
    void register(RegisterDTO dto);

    /**
     * 获取当前用户信息
     */
    User getCurrentUserInfo();

    /**
     * 更新用户信息
     */
    void updateUserInfo(UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(PasswordDTO dto);

    /**
     * 分页查询用户（管理端）
     */
    IPage<User> pageUsers(Integer page, Integer size, String keyword, Integer role, Integer status);

    /**
     * 更新用户状态
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 添加用户（管理端）
     */
    void addUser(User user);

    /**
     * 更新用户（管理端）
     */
    void updateUser(User user);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);
}
