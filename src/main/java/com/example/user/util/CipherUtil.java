package com.example.user.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密工具类
 * 用于加密和解密敏感信息（身份证号、密码等）
 * 密钥从配置文件读取，支持固定密钥和随机密钥两种模式
 */
@Component
public class CipherUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private static SecretKeySpec secretKey;
    private static String keyType;

    @Value("${encryption.aes-key}")
    private String aesKey;

    @Value("${encryption.key-type:FIXED}")
    private String keyTypeConfig;

    /**
     * 初始化加密密钥
     */
    @PostConstruct
    public void init() {
        try {
            keyType = keyTypeConfig;

            if ("RANDOM".equalsIgnoreCase(keyType)) {
                // 随机生成密钥（每次启动服务密钥不同，数据将无法持久化解密）
                KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                keyGenerator.init(256);
                SecretKey key = keyGenerator.generateKey();
                secretKey = new SecretKeySpec(key.getEncoded(), ALGORITHM);
            } else {
                // 使用配置文件中的固定密钥
                byte[] decodedKey = Base64.getDecoder().decode(aesKey);
                secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化加密密钥失败", e);
        }
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 密文（Base64编码）
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param cipherText 密文（Base64编码）
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成新的AES密钥（Base64编码）
     * 用于生成配置文件中的密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }
}
