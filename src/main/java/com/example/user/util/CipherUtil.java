package com.example.user.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CipherUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_KEY = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=";

    private static volatile SecretKeySpec secretKey;
    private static volatile boolean initialized = false;
    private static final Object INIT_LOCK = new Object();

    public static void initialize(String base64Key) {
        // 双重检查锁定：已初始化则无锁快速返回
        if (initialized) {
            return;
        }
        synchronized (INIT_LOCK) {
            // 再次检查，避免多个线程在等待锁期间重复初始化（保持“首次初始化生效”的语义）
            if (initialized) {
                return;
            }
            try {
                byte[] keyBytes = Base64.getDecoder().decode(base64Key);
                // 先发布 secretKey，再置 initialized=true；配合 volatile 保证：
                // 其他线程一旦读到 initialized=true，secretKey 必定已完成构造，杜绝读到 null 的半初始化状态
                secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                initialized = true;
            } catch (Exception e) {
                throw new RuntimeException("初始化加密密钥失败", e);
            }
        }
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize(DEFAULT_KEY);
        }
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        ensureInitialized();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        ensureInitialized();
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
}
