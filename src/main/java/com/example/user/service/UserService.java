package com.example.user.service;

import com.example.user.entity.User;
import com.example.user.entity.dto.UserDTO;
import com.example.user.entity.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(UserDTO userDTO);

    /**
     * 根据ID查询用户
     */
    UserVO getUserById(Long id);

    /**
     * 根据用户名查询用户
     */
    UserVO getUserByUsername(String username);

    /**
     * 查询所有用户
     */
    List<UserVO> getAllUsers();

    /**
     * 查询所有正常状态的用户
     */
    List<UserVO> getAllActiveUsers();

    /**
     * 更新用户信息
     */
    UserVO updateUser(Long id, UserDTO userDTO);

    /**
     * 删除用户（物理删除，仅管理员可操作）
     */
    void deleteUser(Long id);

    /**
     * 注销用户（逻辑删除，用户自己操作）
     */
    void cancelUser(Long id);

    /**
     * 用户登录验证
     */
    UserVO login(String username, String password);
}
