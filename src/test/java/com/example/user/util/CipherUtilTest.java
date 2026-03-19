package com.example.user.util;

/**
 * 加密工具测试类
 * 用于生成AES密钥
 */
public class CipherUtilTest {

    public static void main(String[] args) {
        // 生成新的AES密钥
        String newKey = CipherUtil.generateKey();
        System.out.println("新生成的AES密钥（Base64编码）：");
        System.out.println(newKey);
        System.out.println("\n请将上述密钥配置到application.yml的encryption.aes-key属性中");

        // 测试加密解密
        String testText = "110101199001011234";
        String encrypted = CipherUtil.encrypt(testText);
        String decrypted = CipherUtil.decrypt(encrypted);

        System.out.println("\n加密解密测试：");
        System.out.println("原文：" + testText);
        System.out.println("密文：" + encrypted);
        System.out.println("解密：" + decrypted);
    }
}
