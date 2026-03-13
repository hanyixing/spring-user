package com.example.util;

import com.example.config.CipherConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class CipherUtil {

    @Autowired
    private CipherConfig cipherConfig;

    public String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(cipherConfig.getSecretKey().getBytes(), cipherConfig.getAlgorithm());
        Cipher cipher = Cipher.getInstance(cipherConfig.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(cipherConfig.getSecretKey().getBytes(), cipherConfig.getAlgorithm());
        Cipher cipher = Cipher.getInstance(cipherConfig.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}
