package com.yeepay.g3.sdk.yop.encrypt;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Deprecated
public class BlowfishCipher {

    private final static Logger logger = Logger.getLogger(BlowfishCipher.class);

    private static String CIPHER_NAME = "Blowfish/CFB8/NoPadding";
    private static String KEY_SPEC_NAME = "Blowfish";
    private static String CHARSET = "UTF-8";


    private static Cipher getCipher(String key, boolean encrypt) {
        try {
            String md5Key = Digest.md5Digest(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(md5Key.substring(0, 16).getBytes(), KEY_SPEC_NAME);
            IvParameterSpec ivParameterSpec = new IvParameterSpec((md5Key.substring(0, 8)).getBytes());
            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return cipher;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String data, String key) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(key)) {
            return null;
        }

        try {
            return new String(Base64.encode(getCipher(key, true).doFinal(data.getBytes(CHARSET))), CHARSET);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String data, String key) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(key)) {
            return null;
        }

        try {
            return new String(getCipher(key, false).doFinal(Base64.decode(data.getBytes(CHARSET))), CHARSET);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
