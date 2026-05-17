package com.example.user.entity.vo;

import lombok.Data;

/**
 * 用户视图对象
 * 用于返回给前端，不包含敏感信息
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String maskedIdCard;

    private String phone;

    private String email;

    private Integer status;

    private String role;

    private String createTime;

    private String updateTime;

    private String cancelTime;
}
