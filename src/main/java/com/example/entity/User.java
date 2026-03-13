package com.example.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String idCard;
    private String phone;
    private String email;
    private Integer status;
    private String createTime;
    private String updateTime;
}
