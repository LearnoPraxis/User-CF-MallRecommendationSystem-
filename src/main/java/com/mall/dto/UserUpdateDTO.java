package com.mall.dto;

import lombok.Data;

/**
 * 用户信息更新DTO
 */
@Data
public class UserUpdateDTO {

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;
}
