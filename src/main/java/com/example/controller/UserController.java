package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            User existingUser = userService.getUserByUsername(user.getUsername());
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            User createdUser = userService.createUser(user);
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("data", createdUser);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            User user = userService.getUserByUsername(username);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            if (!user.getPassword().equals(password)) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }
            if (user.getStatus() == 0) {
                result.put("success", false);
                result.put("message", "账户已被注销");
                return result;
            }
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", user);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("data", updatedUser);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            User deletedUser = userService.deleteUser(id);
            result.put("success", true);
            result.put("message", "删除成功");
            result.put("data", deletedUser);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", user);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/{id}/deactivate")
    public Map<String, Object> deactivateUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.deactivateUser(id);
            result.put("success", true);
            result.put("message", "注销成功");
            result.put("data", user);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注销失败: " + e.getMessage());
        }
        return result;
    }
}
