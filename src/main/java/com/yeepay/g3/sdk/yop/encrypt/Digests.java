package com.yeepay.g3.sdk.yop.encrypt;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.yeepay.g3.sdk.yop.utils.Exceptions;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * title: 消息摘要工具类<br>
 * description: <br>
 * 支持SHA-1/MD5，返回byte[]的(可用Encodes进一步被编码为Hex, Base64或UrlSafeBase64),支持带salt达到更高的安全性.<br>
 * 也支持crc32，murmur32这些不追求安全性，性能较高，返回int的.<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/11/23 下午3:34
 */
public class Digests {

    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA512 = "SHA-512";
    public static final String MD5 = "MD5";

    private static SecureRandom random = new SecureRandom();


    private static final Map<String, String> DIGEST_ALG_NAME_CONVERTER = new HashMap<String, String>();

    static {
        DIGEST_ALG_NAME_CONVERTER.put("SHA256", SHA256);
        DIGEST_ALG_NAME_CONVERTER.put("SHA512", SHA512);
    }

    private static String convertDigestAlgNameIfNecessary(String algorithm) {
        algorithm = algorithm.toUpperCase();
        String converted = DIGEST_ALG_NAME_CONVERTER.get(algorithm);
        return converted == null ? algorithm : converted;
    }

    public static String digest2Base64(String input, String algorithm) {
        return Encodes.encodeBase64(digest(input, convertDigestAlgNameIfNecessary(algorithm)));
    }

    public static String digest2Hex(String input, String algorithm) {
        return Hex.toHex(digest(input, convertDigestAlgNameIfNecessary(algorithm)));
    }

    public static byte[] digest(String input, String algorithm) {
        return digest(input, Charsets.UTF_8, algorithm, null, 1);
    }

    public static byte[] digest(String input, String algorithm, byte[] salt) {
        return digest(input, Charsets.UTF_8, algorithm, salt, 1);
    }

    public static byte[] digest(String input, String algorithm, byte[] salt, int iterations) {
        return digest(input, Charsets.UTF_8, algorithm, salt, iterations);
    }

    public static byte[] digest(String input, Charset charset, String algorithm, byte[] salt, int iterations) {
        return digest(input.getBytes(charset), algorithm, salt, iterations);
    }

    /**
     * 对字符串进行散列
     */
    private static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            if (salt != null) {
                digest.update(salt);
            }

            byte[] result = digest.digest(input);

            for (int i = 1; i < iterations; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 生成随机的Byte[]作为salt.
     *
     * @param numBytes salt数组的大小
     */
    public static byte[] generateSalt(int numBytes) {
        Validate.isTrue(numBytes > 0, "numBytes argument must be a positive integer (1 or larger)", numBytes);

        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * 对文件提取摘要.
     */
    public static byte[] digest(InputStream input, String algorithm) throws IOException {
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
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 对输入字符串进行crc32散列.
     */
    public static int crc32(byte[] input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input);
        return (int) crc32.getValue();
    }

    /**
     * 对输入字符串进行crc32散列.
     */
    public static int crc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(Charsets.UTF_8));
        return (int) crc32.getValue();
    }

    /**
     * 对输入字符串进行crc32散列.
     */
    public static int crc32(String input, Charset charset) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(charset));
        return (int) crc32.getValue();
    }

    /**
     * 对输入字符串进行crc32散列，与php兼容，在64bit系统下返回永远是正数的long
     */
    public static long crc32AsLong(byte[] input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input);
        return crc32.getValue();
    }

    /**
     * 对输入字符串进行crc32散列，与php兼容，在64bit系统下返回永远是正数的long
     */
    public static long crc32AsLong(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(Charsets.UTF_8));
        return crc32.getValue();
    }

    /**
     * 对输入字符串进行crc32散列，与php兼容，在64bit系统下返回永远是正数的long
     */
    public static long crc32AsLong(String input, Charset charset) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(charset));
        return crc32.getValue();
    }

    /**
     * 对输入字符串进行murmur32散列
     */
    public static int murmur32(byte[] input) {
        return Hashing.murmur3_32().hashBytes(input).asInt();
    }

    /**
     * 对输入字符串进行murmur32散列
     */
    public static int murmur32(String input) {
        return Hashing.murmur3_32().hashString(input, Charsets.UTF_8).asInt();
    }

    /**
     * 对输入字符串进行murmur32散列
     */
    public static int murmur32(String input, Charset charset) {
        return Hashing.murmur3_32().hashString(input, charset).asInt();
    }

    /**
     * 对输入字符串进行murmur32散列，带有seed
     */
    public static int murmur32(byte[] input, int seed) {
        return Hashing.murmur3_32(seed).hashBytes(input).asInt();
    }

    /**
     * 对输入字符串进行murmur32散列，带有seed
     */
    public static int murmur32(String input, int seed) {
        return Hashing.murmur3_32(seed).hashString(input, Charsets.UTF_8).asInt();
    }

    /**
     * 对输入字符串进行murmur32散列，带有seed
     */
    public static int murmur32(String input, Charset charset, int seed) {
        return Hashing.murmur3_32(seed).hashString(input, charset).asInt();
    }
}
