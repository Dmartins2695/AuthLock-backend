package com.webapp.pwmanager.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Objects;

public class AESUtil {

    public enum DataType {
        HEX,
        BASE64
    }

    private static final Logger LOGGER = LogManager.getLogger(AESUtil.class);

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String KEY_ALGORITHM = "AES";

    private final int IV_SIZE = 128;

    private int iterationCount = 1989;
    private int keySize = 256;

    private int saltLength;

    private final DataType dataType = DataType.BASE64;

    private Cipher cipher;

    public AESUtil() {
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            saltLength = this.keySize / 4;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public AESUtil(int keySize, int iterationCount) {
        this.keySize = keySize;
        this.iterationCount = iterationCount;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            saltLength = this.keySize / 4;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private String encrypt(String salt, String iv, String secret, String strToEncrypt) {
        try {
            SecretKey secretKey = generateKey(salt, secret);
            byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, secretKey, iv, strToEncrypt.getBytes(StandardCharsets.UTF_8));
            String strToDecrypt;

            if (dataType.equals(DataType.HEX)) {
                strToDecrypt = toHex(encrypted);
            } else {
                strToDecrypt = toBase64(encrypted);
            }
            return strToDecrypt;
        } catch (Exception e) {
            return null;
        }
    }

    public String encrypt(String secret, String strToEncrypt) {
        try {
            String salt = toHex(generateRandom(keySize / 8));
            String iv = toHex(generateRandom(IV_SIZE / 8));
            String strToDecrypt = encrypt(salt, iv, secret, strToEncrypt);
            return salt + iv + strToDecrypt;
        } catch (Exception e) {
            return null;
        }
    }

    private String decrypt(String salt, String iv, String secret, String strToDecrypt) {
        try {
            SecretKey key = generateKey(salt, secret);
            byte[] encrypted;
            if (dataType.equals(DataType.HEX)) {
                encrypted = fromHex(strToDecrypt);
            } else {
                encrypted = fromBase64(strToDecrypt);
            }
            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, encrypted);
            return new String(Objects.requireNonNull(decrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public String decrypt(String secret, String strToDecrypt) {
        try {
            String salt = strToDecrypt.substring(0, saltLength);
            int ivLength = IV_SIZE / 4;
            String iv = strToDecrypt.substring(saltLength, saltLength + ivLength);
            String ct = strToDecrypt.substring(saltLength + ivLength);
            return decrypt(salt, iv, secret, ct);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey generateKey(String salt, String secret) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            KeySpec keySpec = new PBEKeySpec(secret.toCharArray(), fromHex(salt), iterationCount, keySize);
            return new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    private static byte[] fromBase64(String str) {
        return DatatypeConverter.parseBase64Binary(str);
    }

    private static String toBase64(byte[] ba) {
        return DatatypeConverter.printBase64Binary(ba);
    }

    private static byte[] fromHex(String str) {
        return DatatypeConverter.parseHexBinary(str);
    }

    private static String toHex(byte[] ba) {
        return DatatypeConverter.printHexBinary(ba);
    }

    private byte[] doFinal(int mode, SecretKey secretKey, String iv, byte[] bytes) {
        try {
            cipher.init(mode, secretKey, new IvParameterSpec(fromHex(iv)));
            return cipher.doFinal(bytes);
        } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
                 | InvalidKeyException e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    private static byte[] generateRandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return randomBytes;
    }

}