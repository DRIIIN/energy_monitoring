package com.energy.monitoring.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energy.monitoring.config.Config;
import com.energy.monitoring.config.ConfigKeys;

/* Инструменты для хеширования паролей */
public class PasswordHasher {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);  // Объект Logger для текущего класса
    
    private static final int    ITERATIONS  = Config.getInt(ConfigKeys.PasswordHasher.ITERATIONS);
    private static final int    KEY_LENGTH  = Config.getInt(ConfigKeys.PasswordHasher.KEY_LENGTH);
    private static final String ALGORITHM   = Config.getString(ConfigKeys.PasswordHasher.ALGORITHM);
    private static final int    SALT_LENGTH = Config.getInt(ConfigKeys.PasswordHasher.SALT_LENGTH);
    
    // Возвращает зашифрованный пароль password
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt         = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

                try {
                    byte[] hash       = factory.generateSecret(spec).getEncoded();
                    String saltBase64 = Base64.getEncoder().encodeToString(salt);
                    String hashBase64 = Base64.getEncoder().encodeToString(hash);
                    
                    return String.format("%s:%s", saltBase64, hashBase64);
                } catch (InvalidKeySpecException e) {
                    logger.error("Error hashing password: {}", e.getMessage());
                    throw new RuntimeException("Password hashing failed", e);
                }
            } catch (NoSuchAlgorithmException e) {
                logger.error("Algorithm-error hashing password: {}", e.getMessage());
                throw new RuntimeException("Password hashing algorithm failed", e);
            }

        } catch (RuntimeException  e) {
            logger.error("Runtime-error hashing password: {}", e.getMessage());
            throw new RuntimeException("Password hashing runtime failed", e);
        }
    }
    
    // Возвращает true если пароль верефицируется, иначе - false
    public static boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        } else {
            try {
                byte[]  salt            = Base64.getDecoder().decode(parts[0]);
                byte[]  storedHashBytes = Base64.getDecoder().decode(parts[1]);
                KeySpec spec            = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

                try {
                    SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

                    try {
                        byte[] testHash = factory.generateSecret(spec).getEncoded();

                        return constantTimeEquals(storedHashBytes, testHash);
                    } catch (InvalidKeySpecException e) {
                        logger.error("Error verifying password: {}", e.getMessage());
                        return false;
                    }

                } catch (NoSuchAlgorithmException e) {
                    logger.error("Error verifying password: {}", e.getMessage());
                    return false;
                }

            } catch (IllegalArgumentException e) {
                logger.error("Error verifying password: {}", e.getMessage());
                return false;
            }
        }
    }
    
    // Возвращает true, если хэш коректен, иначе - false
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}