package org.example.bankcards.util;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.exception.DecryptSecretException;
import org.example.bankcards.exception.EncryptSecretException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class SecurityUtil {

    private static final String ALGO_AES = "AES";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public static String generateJwtSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    public static String encryptSecret(String secret, String masterKey)
            throws EncryptSecretException {
        try {
            Cipher cipher = Cipher.getInstance(ALGO_AES);
            SecretKey secretKey = getAesSecretKey(masterKey);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return ENCODER.encodeToString(cipher.doFinal(secret.getBytes()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EncryptSecretException(e);
        }
    }

    public static String decryptSecret(String encryptedSecret, String masterKey)
            throws DecryptSecretException{
        try {
            Cipher cipher = Cipher.getInstance(ALGO_AES);
            SecretKey secretKey = getAesSecretKey(masterKey);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(DECODER.decode(encryptedSecret.getBytes())));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DecryptSecretException(e);
        }
    }

    public static SecretKey getAesSecretKey(String masterKey) {
        return new SecretKeySpec(masterKey.getBytes(), "AES");
    }

    public static SecretKey getHmacShaSecretKey(String masterKey) {
        return Keys.hmacShaKeyFor(masterKey.getBytes());
    }
}
