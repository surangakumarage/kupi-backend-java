package com.kupi.notification.util;

import com.kupi.notification.exception.NotificationServiceException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class TokenEncoder {

    private static final String SECRET = "CC-56d748e2-085b-455f-98db-3a57cdbf532e";
    private static final String UTF_8 = "UTF-8";
    private static final String SHA_1 = "SHA-1";
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_LENGTH = 16;
    private final SecretKeySpec secretKey;

    public TokenEncoder() {
        this.secretKey = generateSecret();
    }

    /**
     * This method encrypts the given string.
     *
     * @param strToEncrypt String to encrypt
     * @return encrypted string
     */
    public String encode(String strToEncrypt) {
        try {
            Cipher cipher = generateCipherInstance();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(UTF_8)));
        } catch (InvalidKeyException | UnsupportedEncodingException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new NotificationServiceException("String encryption was failed.", e);
        }
    }

    /**
     * This method decrypts the given string.
     *
     * @param strToDecrypt String to decrypt
     * @return decrypted string
     */
    public String decode(String strToDecrypt) {
        try {
            Cipher cipher = generateCipherInstance();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new NotificationServiceException("String decryption was failed.", e);
        }
    }

    /**
     * This method generates CipherInstance
     *
     * @return CipherInstance
     */
    private Cipher generateCipherInstance() {
        try {
            return Cipher.getInstance(CIPHER_TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new NotificationServiceException("Generating Cipher instance was failed.", e);
        }
    }

    /**
     * This method generates the secret key
     *
     * @return secret key
     */
    private SecretKeySpec generateSecret() {
        try {
            byte[] key = SECRET.getBytes(UTF_8);
            MessageDigest sha = MessageDigest.getInstance(SHA_1);
            key = sha.digest(key);
            key = Arrays.copyOf(key, KEY_LENGTH);
            return new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new NotificationServiceException("Setting secret was failed.", e);
        }
    }
}
