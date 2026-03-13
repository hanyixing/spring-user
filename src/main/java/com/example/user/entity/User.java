package com.example.user.entity;

import com.example.user.util.CipherUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 包含用户身份信息，身份证号和密码使用Cipher类加密存储
 */
@Data
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（加密存储）
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 身份证号（加密存储）
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态：0-正常，1-已注销
     */
    @NotNull(message = "用户状态不能为空")
    private Integer status;

    /**
     * 用户角色：USER-普通用户，ADMIN-管理员
     */
    @NotBlank(message = "用户角色不能为空")
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 注销时间
     */
    private LocalDateTime cancelTime;

    /**
     * 获取解密后的身份证号
     */
    public String getDecryptedIdCard() {
        return CipherUtil.decrypt(this.idCard);
    }

    /**
     * 设置加密后的身份证号
     */
    public void setEncryptedIdCard(String idCard) {
        this.idCard = CipherUtil.encrypt(idCard);
    }

    /**
     * 获取解密后的密码
     */
    public String getDecryptedPassword() {
        return CipherUtil.decrypt(this.password);
    }

    /**
     * 设置加密后的密码
     */
    public void setEncryptedPassword(String password) {
        this.password = CipherUtil.encrypt(password);
    }
}
