package com.mall.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 角色：0-普通用户 1-管理员 */
    private Integer role;
}
