package com.example.user.mapper;

import com.example.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询所有用户
     */
    List<User> selectAll();

    /**
     * 查询指定角色级别的用户（用于权限控制）
     */
    List<User> selectByRoleLevel(@Param("maxRoleLevel") int maxRoleLevel);

    /**
     * 查询所有正常状态的用户
     */
    List<User> selectAllActive();

    /**
     * 新增用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int update(User user);

    /**
     * 删除用户（物理删除，仅管理员可操作）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 注销用户（逻辑删除，用户自己操作）
     */
    int cancelUser(@Param("id") Long id);

    /**
     * 检查用户名是否存在
     */
    int checkUsernameExists(@Param("username") String username);

    /**
     * 检查身份证号是否存在
     */
    int checkIdCardExists(@Param("idCard") String idCard);
}
