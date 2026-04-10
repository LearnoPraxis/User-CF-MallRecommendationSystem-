package com.mall.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        LoginUser user = getCurrentUser();
        return user != null && user.getRole() == 1;
    }
}
