package com.example.user.mapper;

import com.example.user.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LoginLogMapper {

    void insert(LoginLog loginLog);

    LoginLog selectById(Long id);

    List<LoginLog> selectByUserId(Long userId);

    List<LoginLog> selectAll();
}
