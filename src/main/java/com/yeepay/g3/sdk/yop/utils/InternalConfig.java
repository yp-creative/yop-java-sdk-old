package com.yeepay.g3.sdk.yop.utils;

import com.google.common.collect.Maps;
import com.yeepay.g3.facade.yop.ca.enums.CertTypeEnum;
import com.yeepay.g3.facade.yop.ca.enums.KeyStoreTypeEnum;
import com.yeepay.g3.frame.yop.ca.rsa.RSAKeyUtils;
import com.yeepay.g3.frame.yop.ca.utils.Exceptions;
import com.yeepay.g3.sdk.yop.config.CertConfig;
import com.yeepay.g3.sdk.yop.config.CertStoreType;
import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Map;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:48
 */
public final class InternalConfig {

    private static final Logger LOGGER = Logger.getLogger(InternalConfig.class);

    private static final String SP_SDK_CONFIG_FILE = "yop.sdk.config.file";

    /**
     * 1.如果配置file://开头，则是系统绝对路径
     * 2.如果是/开头，则是classpath下相对路径
     */
    private static final String DEFAULT_SDK_CONFIG_FILE = "/config/yop_sdk_config_default.json";

    public static final String PROTOCOL_VERSION = "yop-auth-v2";

    private static final String CONFIG_FILE_ABSOLUTE_PATH_PREFIX = "file://";
    private static final String CONFIG_FILE_CLASS_PATH_PREFIX = "classpath:";
    private static final String SLASH = "/";
    public static String APP_KEY;
    public static String SECRET_KEY;

    public static String SERVER_ROOT;

    public static int CONNECT_TIMEOUT = 30000;
    public static int READ_TIMEOUT = 60000;

    public static int MAX_CONN_TOTAL = 200;
    public static int MAX_CONN_PER_ROUTE = 100;

    public static boolean TRUST_ALL_CERTS = false;

    private static Map<CertTypeEnum, PublicKey> yopPublicKeyMap = Maps.newEnumMap(CertTypeEnum.class);

    private static Map<CertTypeEnum, PrivateKey> isvPrivateKeyMap = Maps.newEnumMap(CertTypeEnum.class);

    static {
        initialize();
    }

    private InternalConfig() {
        /*forbid instantiate*/
    }

    private static void initialize() {
        LOGGER.info("[yop-sdk]尝试加载配置，具体使用请参照https://open.yeepay.com/doc/platform_profile/java_sdk_guide");

        String configFilePath = System.getProperty(SP_SDK_CONFIG_FILE);
        if (StringUtils.isBlank(configFilePath)) {
            LOGGER.info("[yop-sdk]系统属性yop.sdk.config.file未配置，将尝试从默认路径classpath:" + DEFAULT_SDK_CONFIG_FILE + "寻找配置文件");
            configFilePath = DEFAULT_SDK_CONFIG_FILE;
        }

        LOGGER.info(String.format("[yop-sdk]尝试加载配置文件%s，将在%s下寻找该文件", configFilePath, configFilePath.startsWith(CONFIG_FILE_ABSOLUTE_PATH_PREFIX) ? "绝对路径" : "classpath"));

        InputStream configInputStream = getInputStream(configFilePath);
        if (configInputStream == null) {
            LOGGER.warn("[yop-sdk]未找到或无权限访问配置文件" + configFilePath + "，将不使用配置文件，请在代码中手动传递所需配置");
            return;
        }

        SDKConfig config;
        try {
            config = JsonUtils.loadFrom(configInputStream, SDKConfig.class);
        } catch (Exception e) {
            LOGGER.error("[yop-sdk]从配置文件加载配置时失败，请检查配置文件格式", e);
            throw Exceptions.unchecked(e);
        } finally {
            close(configInputStream);
        }

        SERVER_ROOT = config.getServerRoot();
        APP_KEY = config.getAppKey();
        SECRET_KEY = config.getAesSecretKey();

        // HttpClient 配置
        if (config.getConnectTimeout() != null && config.getConnectTimeout() >= 0) {
            CONNECT_TIMEOUT = config.getConnectTimeout();
        }
        if (config.getReadTimeout() != null && config.getReadTimeout() >= 0) {
            READ_TIMEOUT = config.getReadTimeout();
        }
        if (config.getMaxConnTotal() != null && config.getMaxConnTotal() >= 0) {
            MAX_CONN_TOTAL = config.getMaxConnTotal();
        }
        if (config.getMaxConnPerRoute() != null && config.getMaxConnPerRoute() >= 0) {
            MAX_CONN_PER_ROUTE = config.getMaxConnPerRoute();
        }

        // 信任所有证书
        if (config.getTrustAllCerts() != null) {
            TRUST_ALL_CERTS = config.getTrustAllCerts();
        } else {
            TRUST_ALL_CERTS = Boolean.valueOf(System.getProperty("yop.sdk.trust.all.certs", "false"));
        }

        if (null != config.getYopPublicKey()) {
            for (CertConfig certConfig : config.getYopPublicKey()) {
                yopPublicKeyMap.put(certConfig.getCertType(), loadPublicKey(certConfig));
            }
        }

        if (null != config.getIsvPrivateKey()) {
            for (CertConfig certConfig : config.getIsvPrivateKey()) {
                isvPrivateKeyMap.put(certConfig.getCertType(), loadPrivateKey(certConfig));
            }
        }

        LOGGER.info("[yop-sdk]已成功从配置文件" + configFilePath + "中加载所有配置");
    }

    private static PublicKey loadPublicKey(CertConfig certConfig) {
        LOGGER.info("[yop-sdk]尝试从配置文件中加载yop平台公钥");
        CertStoreType certStoreType = certConfig.getStoreType();
        if (certStoreType != CertStoreType.STRING) {
            throw new YopClientException("[yop-sdk]初始化yop平台公钥异常! 公钥[yop_public_key.store_type]配置有误，公钥[store_type]的合法值为string.");
        }
        if (StringUtils.isBlank(certConfig.getValue())) {
            throw new YopClientException("[yop-sdk]初始化yop平台公钥异常！公钥[yop_public_key.value]不能为空！");
        }
        try {
            PublicKey publicKey = RSAKeyUtils.string2PublicKey(certConfig.getValue());
            LOGGER.info("[yop-sdk]从配置文件中加载yop平台公钥成功");
            return publicKey;
        } catch (Exception e) {
            LOGGER.error("[yop-sdk]初始化yop平台公钥异常!请检查配置项[yop_public_key.value]", e);
            throw Exceptions.unchecked(e);
        }
    }

    private static PrivateKey loadPrivateKey(CertConfig certConfig) {
        LOGGER.info("[yop-sdk]尝试从配置文件中加载用户私钥");
        PrivateKey privateKey;
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("[yop-sdk]初始化用户私钥异常! 私钥[isv_private_key.store_type]配置有误，私钥[store_type]的合法值为string或file_p12");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    privateKey = RSAKeyUtils.string2PrivateKey(certConfig.getValue());
                } catch (Exception e) {
                    LOGGER.error("[yop-sdk]初始化string类型用户私钥异常!请检查配置项[isv_private_key.value]", e);
                    throw Exceptions.unchecked(e);
                }
                break;
            case FILE_P12:
                if (StringUtils.isBlank(certConfig.getPassword())) {
                    throw new YopClientException("[yop-sdk]初始化file_p12类型用户私钥异常！[isv_private_key.password]不能为空");
                }
                char[] password = certConfig.getPassword().toCharArray();

                if (StringUtils.isBlank(certConfig.getValue())) {
                    throw new YopClientException("[yop-sdk]初始化file_p12类型用户私钥异常！[isv_private_key.value]不能为空");
                }

                LOGGER.info("[yop-sdk]尝试从p12文件" + certConfig.getValue() + "中解析私钥");
                InputStream certInputStream = getInputStream(certConfig.getValue());
                if (certInputStream == null) {
                    throw new YopClientException("[yop-sdk]初始化file_p12类型用户私钥异常！找不到文件:" + certConfig.getValue() + ",请将文件放在项目classpath下，并以classpath:为前缀指定路径；或者放在服务器绝对路径下，并以file:/为前缀指定路径");
                }

                try {
                    KeyStore keystore = KeyStore.getInstance(KeyStoreTypeEnum.PKCS12.getValue());
                    keystore.load(certInputStream, password);

                    Enumeration aliases = keystore.aliases();
                    String keyAlias = "";
                    while (aliases.hasMoreElements()) {
                        keyAlias = (String) aliases.nextElement();
                    }
                    privateKey = (PrivateKey) keystore.getKey(keyAlias, password);
                    LOGGER.info("[yop-sdk]从p12文件" + certConfig.getValue() + "中解析私钥成功");
                } catch (Exception e) {
                    LOGGER.error("[yop-sdk]初始化file_p12类型用户私钥异常!", e);
                    throw Exceptions.unchecked(e);
                } finally {
                    close(certInputStream);
                }
                break;
            default:
                throw new YopClientException("[yop-sdk]暂不支持配置该密钥类型:" + certConfig.getStoreType());
        }
        LOGGER.info("[yop-sdk]从配置文件中加载用户私钥成功");
        return privateKey;
    }

    public static InputStream getInputStream(String location) {
        InputStream fis;
        if (StringUtils.startsWith(location, CONFIG_FILE_ABSOLUTE_PATH_PREFIX)) {
            try {
                fis = new FileInputStream(StringUtils.substring(location, 6));
                return fis;
            } catch (FileNotFoundException e) {
                LOGGER.error("[yop-sdk]文件" + StringUtils.substring(location, 6) + "不存在，或者无访问权限");
                return null;
            }
        }
        if (StringUtils.startsWith(location, CONFIG_FILE_CLASS_PATH_PREFIX)) {
            location = StringUtils.substring(location, CONFIG_FILE_CLASS_PATH_PREFIX.length());
        }
        if (!StringUtils.startsWith(location, SLASH)) {
            location = SLASH + location;
        }

        return InternalConfig.class.getResourceAsStream(location);
    }

    public static PublicKey getYopPublicKey(CertTypeEnum certType) {
        return yopPublicKeyMap.get(certType);
    }

    public static PrivateKey getISVPrivateKey(CertTypeEnum certType) {
        return isvPrivateKeyMap.get(certType);
    }

    private static void close(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
