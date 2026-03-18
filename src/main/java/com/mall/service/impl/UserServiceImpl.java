package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.LoginDTO;
import com.mall.dto.PasswordDTO;
import com.mall.dto.RegisterDTO;
import com.mall.dto.UserUpdateDTO;
import com.mall.entity.User;
import com.mall.exception.BusinessException;
import com.mall.mapper.UserMapper;
import com.mall.security.JwtTokenUtil;
import com.mall.security.SecurityUtil;
import com.mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        // 生成Token
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StrUtil.isNotBlank(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(0); // 普通用户
        user.setStatus(1); // 正常状态
        this.save(user);
    }

    @Override
    public User getCurrentUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return this.getById(userId);
    }

    @Override
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = new User();
        user.setId(userId);
        BeanUtil.copyProperties(dto, user);
        this.updateById(user);
    }

    @Override
    public void updatePassword(PasswordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = this.getById(userId);
        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(updateUser);
    }

    @Override
    public IPage<User> pageUsers(Integer page, Integer size, String keyword, Integer role, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        this.removeById(userId);
    }

    @Override
    public void addUser(User user) {
        // 检查用户名是否存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRole() == null) {
            user.setRole(0);
        }
        this.save(user);
    }

    @Override
    public void updateUser(User user) {
        // 检查用户名是否被其他用户占用
        if (StrUtil.isNotBlank(user.getUsername())) {
            long count = this.count(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
                    .ne(User::getId, user.getId()));
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
        }
        // 不更新密码字段
        user.setPassword(null);
        this.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }
}
