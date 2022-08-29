package com.webapp.pwmanager.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityCipher {

    @Value("${jwt.auth.key_value}")
    private String keyValue;

    private final AESUtil aesUtil = new AESUtil();

    public String encrypt(String strToEncrypt) {
        String encrypted = aesUtil.encrypt(keyValue,strToEncrypt);
        return encrypted;
    }


    public String decrypt(String strToDecrypt) {
       return aesUtil.decrypt(keyValue,strToDecrypt);
    }
}
