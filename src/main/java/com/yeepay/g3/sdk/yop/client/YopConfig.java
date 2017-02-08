package com.yeepay.g3.sdk.yop.client;


/**
 * <pre>
 * 系统级参数的名称
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopConfig {

    private static String serverRoot = "https://open.yeepay.com/yop-center";

    /**
     * 开放应用名
     */
    private static String appKey;

    /**
     * AES密钥，注册开放应用时获取的密钥<br>
     * 对应使用AES算法加解密
     */
    private static String aesSecretKey;

    /**
     * Hmac密钥，即易宝商户入网后所得的HmacKey<br>
     * 非开放应用身份发起调用时（即不提供appKey时）使用此密钥签名及报文加密<br>
     * 对应使用Blowfish算法加解密
     */
    private static String hmacSecretKey;

    /**
     * 连接超时时间
     */
    private static int connectTimeout = 10000;

    /**
     * 读取返回结果超时
     */
    private static int readTimeout = 30000;

    public static String getAppKey() {
        return appKey;
    }

    public static void setAppKey(String appKey) {
        YopConfig.appKey = appKey;
    }

    public static String getAesSecretKey() {
        return aesSecretKey;
    }

    public static void setAesSecretKey(String aesSecretKey) {
        YopConfig.aesSecretKey = aesSecretKey;
    }

    public static String getHmacSecretKey() {
        return hmacSecretKey;
    }

    public static void setHmacSecretKey(String hmacSecretKey) {
        YopConfig.hmacSecretKey = hmacSecretKey;
    }

    public static String getServerRoot() {
        return serverRoot;
    }

    public static void setServerRoot(String serverRoot) {
        YopConfig.serverRoot = serverRoot;
    }

    public static int getConnectTimeout() {
        return connectTimeout;
    }

    public static void setConnectTimeout(int connectTimeout) {
        YopConfig.connectTimeout = connectTimeout;
    }

    public static int getReadTimeout() {
        return readTimeout;
    }

    public static void setReadTimeout(int readTimeout) {
        YopConfig.readTimeout = readTimeout;
    }

    /**
     * 已设置appKey，默认使用AES密钥
     */
    public static String getSecret() {
        if (appKey != null && appKey.trim().length() > 0) {
            return aesSecretKey;
        } else {
            return hmacSecretKey;
        }
    }
}
