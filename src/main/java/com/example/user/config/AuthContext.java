package com.example.user.config;

import com.example.user.entity.Role;
import com.example.user.entity.User;
import com.example.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
@RequiredArgsConstructor
public class AuthContext {

    private final UserMapper userMapper;

    private static final String USER_ID_KEY = "userId";

    public void setLoginUser(Long userId) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_ID_KEY, userId);
        }
    }

    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object userId = session.getAttribute(USER_ID_KEY);
                if (userId instanceof Long) {
                    return (Long) userId;
                }
            }
        }
        return null;
    }

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            return userMapper.selectById(userId);
        }
        return null;
    }

    public Role getCurrentRole() {
        User user = getCurrentUser();
        if (user != null && user.getRole() != null) {
            try {
                return Role.valueOf(user.getRole());
            } catch (IllegalArgumentException e) {
                return Role.USER;
            }
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getCurrentUserId() != null;
    }

    public boolean isAdmin() {
        Role role = getCurrentRole();
        return role != null && role.isAdmin();
    }

    public boolean isSuperAdmin() {
        Role role = getCurrentRole();
        return role != null && role.isSuperAdmin();
    }

    public boolean canManage(Role targetRole) {
        Role currentRole = getCurrentRole();
        if (currentRole == null) {
            return false;
        }
        return currentRole.canManage(targetRole);
    }

    public boolean canManage(String targetRoleStr) {
        try {
            Role targetRole = Role.valueOf(targetRoleStr);
            return canManage(targetRole);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
