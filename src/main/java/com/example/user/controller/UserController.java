package com.example.user.controller;

import com.example.user.entity.dto.UserDTO;
import com.example.user.entity.vo.UserVO;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 提供用户CRUD、注销等功能
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Validated @RequestBody UserDTO userDTO) {
        UserVO userVO = userService.register(userDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "注册成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        UserVO userVO = userService.login(username, password);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        UserVO userVO = userService.getUserByUsername(username);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有用户（包含已注销用户）
     * 仅管理员可操作
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserVO> users = userService.getAllUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", users);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有正常状态的用户
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveUsers() {
        List<UserVO> users = userService.getAllActiveUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", users);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Validated @RequestBody UserDTO userDTO) {
        UserVO userVO = userService.updateUser(id, userDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除用户（物理删除）
     * 仅管理员可操作
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 注销用户（逻辑删除）
     * 用户自己操作
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelUser(@PathVariable Long id) {
        userService.cancelUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "注销成功");
        return ResponseEntity.ok(result);
    }
}
