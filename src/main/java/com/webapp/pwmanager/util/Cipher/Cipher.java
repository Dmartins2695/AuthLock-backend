package com.webapp.pwmanager.util.Cipher;


import org.springframework.beans.factory.annotation.Value;

public class Cipher {
    @Value("${jwt.auth.key_value}")
    private String keyValue;

    public String getKeyValue() {
        return keyValue;
    }
}
