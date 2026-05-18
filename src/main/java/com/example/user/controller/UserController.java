package com.example.user.controller;

import com.example.user.config.AuthContext;
import com.example.user.entity.LoginLog;
import com.example.user.entity.Role;
import com.example.user.entity.User;
import com.example.user.entity.dto.UserDTO;
import com.example.user.entity.vo.UserVO;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthContext authContext;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Validated @RequestBody UserDTO userDTO) {
        UserVO userVO = userService.register(userDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "注册成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        UserVO userVO = userService.login(username, password, ipAddress);
        
        authContext.setLoginUser(userVO.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登出成功");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        UserVO userVO = userService.getUserByUsername(username);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        if (!authContext.isAdmin()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }
        
        Role currentRole = authContext.getCurrentRole();
        List<UserVO> users;
        
        if (currentRole == Role.SUPER_ADMIN) {
            users = userService.getAllUsers();
        } else {
            users = userService.getUsersByRoleLevel(currentRole.getLevel());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", users);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveUsers() {
        List<UserVO> users = userService.getAllActiveUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", users);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Validated @RequestBody UserDTO userDTO) {
        User targetUser = userService.getUserEntityById(id);
        if (targetUser == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "用户不存在");
            return ResponseEntity.status(404).body(result);
        }
        
        Long currentUserId = authContext.getCurrentUserId();
        boolean isSelf = currentUserId != null && currentUserId.equals(id);
        
        if (!isSelf && !authContext.canManage(targetUser.getRole())) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足，无法修改该用户");
            return ResponseEntity.status(403).body(result);
        }
        
        UserVO userVO = userService.updateUser(id, userDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", userVO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        User targetUser = userService.getUserEntityById(id);
        if (targetUser == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "用户不存在");
            return ResponseEntity.status(404).body(result);
        }
        
        if (!authContext.canManage(targetUser.getRole())) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足，无法删除该用户");
            return ResponseEntity.status(403).body(result);
        }
        
        Long currentUserId = authContext.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", "不能删除自己");
            return ResponseEntity.status(400).body(result);
        }
        
        userService.deleteUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelUser(@PathVariable Long id) {
        Long currentUserId = authContext.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(id)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "只能注销自己的账户");
            return ResponseEntity.status(403).body(result);
        }
        
        userService.cancelUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "注销成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        User targetUser = userService.getUserEntityById(id);
        if (targetUser == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "用户不存在");
            return ResponseEntity.status(404).body(result);
        }
        
        Long currentUserId = authContext.getCurrentUserId();
        boolean isSelf = currentUserId != null && currentUserId.equals(id);
        
        if (!isSelf && !authContext.canManage(targetUser.getRole())) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足，无法修改该用户密码");
            return ResponseEntity.status(403).body(result);
        }
        
        userService.updatePassword(id, newPassword);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "密码修改成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestParam String newRole) {
        User targetUser = userService.getUserEntityById(id);
        if (targetUser == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "用户不存在");
            return ResponseEntity.status(404).body(result);
        }
        
        if (!authContext.canManage(targetUser.getRole())) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足，无法修改该用户角色");
            return ResponseEntity.status(403).body(result);
        }
        
        try {
            Role targetRole = Role.valueOf(newRole);
            if (!authContext.canManage(targetRole)) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 403);
                result.put("message", "权限不足，无法将用户角色修改为 " + newRole);
                return ResponseEntity.status(403).body(result);
            }
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", "无效的角色: " + newRole);
            return ResponseEntity.status(400).body(result);
        }
        
        userService.updateRole(id, newRole);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "角色修改成功");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/login-logs")
    public ResponseEntity<Map<String, Object>> getLoginLogs(@PathVariable Long id) {
        List<LoginLog> logs = userService.getLoginLogs(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/login-logs")
    public ResponseEntity<Map<String, Object>> getAllLoginLogs() {
        List<LoginLog> logs = userService.getAllLoginLogs();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs);
        return ResponseEntity.ok(result);
    }
}
