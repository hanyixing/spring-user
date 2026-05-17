package com.example.user.entity;

import com.example.user.util.CipherUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户实体类
 * 包含用户身份信息，身份证号和密码使用Cipher类加密存储
 */
@Data
public class User {

    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private String email;
    
    @NotNull(message = "用户状态不能为空")
    private Integer status;
    
    @NotBlank(message = "用户角色不能为空")
    private String role;
    
    private String createTime;
    
    private String updateTime;
    
    private String cancelTime;

    public String getDecryptedIdCard() {
        return CipherUtil.decrypt(this.idCard);
    }

    public void setEncryptedIdCard(String idCard) {
        this.idCard = CipherUtil.encrypt(idCard);
    }

    public String getDecryptedPassword() {
        return CipherUtil.decrypt(this.password);
    }

    public void setEncryptedPassword(String password) {
        this.password = CipherUtil.encrypt(password);
    }
}
