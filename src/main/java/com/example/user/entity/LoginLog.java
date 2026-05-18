package com.example.user.entity;

import lombok.Data;

@Data
public class LoginLog {
    private Long id;
    private Long userId;
    private String username;
    private String loginTime;
    private String ipAddress;
    private Integer loginStatus;
    private String message;
}
