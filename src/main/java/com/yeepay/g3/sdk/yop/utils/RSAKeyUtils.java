package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.encrypt.Encodes;
import com.yeepay.g3.sdk.yop.encrypt.RSAKeyPair;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.regex.Pattern;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/24 下午2:11
 */
public class RSAKeyUtils {

    private static final String RSA = "RSA";

    private static final Pattern PUB_KEY_WITH_HEADER_AND_TRAILER_PATTERN =
            Pattern.compile("^-----BEGIN (RSA )?PUBLIC KEY-----(([\r\n](.{32}[\r\n])?((.*[\r\n])*))|(.*))-----END (RSA )?PUBLIC KEY-----$");

    /**
     * string 转 java.security.PublicKey
     *
     * @param pubKey pubKey
     * @return PublicKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey string2PublicKey(String pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(RSA).generatePublic(
                new X509EncodedKeySpec(Encodes.decodeBase64(pubKey)));
    }

    public static PrivateKey string2PrivateKey(String priKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(RSA).generatePrivate(
                new PKCS8EncodedKeySpec(Encodes.decodeBase64(priKey)));
    }

    public static String key2String(Key key) {
        return Encodes.encodeBase64(key.getEncoded());
    }

    public static RSAKeyPair generateKeyPair() {
        return generateKeyPair(2048);
    }

    /**
     * 生成rsa密钥对
     *
     * @param keySize keySize
     * @return RSAKeyPair
     */
    public static RSAKeyPair generateKeyPair(int keySize) {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance(RSA);
        } catch (NoSuchAlgorithmException e) {
            //should never be here...
            e.printStackTrace();
        }
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKeyPair pair = new RSAKeyPair();
        pair.setPublicKey(publicKey);
        pair.setPrivateKey(privateKey);
        pair.setPublicKeyString(key2String(publicKey));
        pair.setPrivateKeyString(key2String(privateKey));
        return pair;
    }

}
