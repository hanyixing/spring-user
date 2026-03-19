package com.example.user.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 * 用于返回给前端，不包含敏感信息
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号（脱敏显示）
     */
    private String maskedIdCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态：0-正常，1-已注销
     */
    private Integer status;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 注销时间
     */
    private LocalDateTime cancelTime;
}
