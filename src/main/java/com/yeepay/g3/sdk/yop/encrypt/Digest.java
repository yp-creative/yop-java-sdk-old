/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.encrypt;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.exception.YopClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 * 计算摘要的工具类
 *
 * @author：junning.li
 * @since：2011-6-1 下午06:07:38
 * @version:
 */
public class Digest {

    private static final String SHA1 = "SHA-1";
    private static final String MD5 = "MD5";

    /**
     * 使用MD5算法计算摘要，并对结果进行hex转换
     *
     * @param input 源数据
     * @return 摘要信息
     */
    public static String md5Digest(String input) {
        return digest(input, MD5);
    }

    /**
     * 使用SHA-0算法计算摘要，并对结果进行hex转换
     *
     * @param input 源数据
     * @return 摘要信息
     */
    public static String sha1Digest(String input) {
        return digest(input, "SHA1");
    }

    public static String sha256Hex(String signingKey, String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey.getBytes(YopConstants.ENCODING), "HmacSHA256"));
            return new String(Hex.toHex(mac.doFinal(input.getBytes(YopConstants.ENCODING))));
        } catch (Exception e) {
            throw new YopClientException("Fail to generate the signature", e);
        }
    }

    /**
     * 对文件进行md5散列
     */
    public static byte[] md5(InputStream input) throws IOException {
        return digest(input, MD5);
    }

    /**
     * 对文件进行sha1散列
     */
    public static byte[] sha1(InputStream input) throws IOException {
        return digest(input, SHA1);
    }

    private static byte[] digest(InputStream input, String algorithm) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            int bufferLength = 8 * 1024;
            byte[] buffer = new byte[bufferLength];
            int read = input.read(buffer, 0, bufferLength);

            while (read > -1) {
                messageDigest.update(buffer, 0, read);
                read = input.read(buffer, 0, bufferLength);
            }

            return messageDigest.digest();
        } catch (GeneralSecurityException e) {
            throw new YopClientException("Fail to generate the signature", e);
        }
    }

    /**
     * 根据指定算法计算摘要
     *
     * @param input     源数据
     * @param algorithm 摘要算法
     * @return 摘要信息
     */
    public static String digest(String input, String algorithm) {
        try {
            byte[] data = input.getBytes(YopConstants.ENCODING);
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return Hex.toHex(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException("digest fail!", e);
        }
    }
}
