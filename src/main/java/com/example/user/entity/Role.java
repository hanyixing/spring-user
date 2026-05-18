package com.example.user.entity;

import lombok.Getter;

@Getter
public enum Role {
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员", 3),
    ADMIN("ADMIN", "管理员", 2),
    USER("USER", "普通用户", 1);

    private final String code;
    private final String description;
    private final int level;

    Role(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    public static Role fromCode(String code) {
        for (Role role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return USER;
    }

    public boolean canManage(Role targetRole) {
        return this.level > targetRole.level;
    }

    public boolean isAdmin() {
        return this.level >= ADMIN.level;
    }

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }
}
