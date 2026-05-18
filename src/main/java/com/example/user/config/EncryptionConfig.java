package com.example.user.config;

import com.example.user.util.CipherUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class EncryptionConfig {

    @Value("${encryption.aes-key}")
    private String aesKey;

    @PostConstruct
    public void init() {
        CipherUtil.initialize(aesKey);
        System.out.println("加密工具初始化完成");
    }
}
