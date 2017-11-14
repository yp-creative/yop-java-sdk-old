package com.yeepay.g3.sdk.yop.utils;

import com.google.common.collect.Maps;
import com.yeepay.g3.facade.yop.ca.enums.CertTypeEnum;
import com.yeepay.g3.facade.yop.ca.enums.KeyStoreTypeEnum;
import com.yeepay.g3.frame.yop.ca.rsa.RSAKeyUtils;
import com.yeepay.g3.frame.yop.ca.utils.Exceptions;
import com.yeepay.g3.sdk.yop.config.CertConfig;
import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.utils.common.log.Logger;
import com.yeepay.g3.utils.common.log.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalConfig.class);

    private static final String SP_SDK_CONFIG_FILE = "yop.sdk.config.file";

    /**
     * 1.如果配置file://开头，则是系统绝对路径
     * 2.如果是/开头，则是classpath下相对路径
     */
    private static final String DEFAULT_SDK_CONFIG_FILE = "/config/yop_sdk_config_default.json";

    public static final String PROTOCOL_VERSION = "yop-auth-v2";

    public static String APP_KEY;
    public static String SECRET_KEY;

    public static String SERVER_ROOT;

    public static int CONNECT_TIMEOUT = 30000;
    public static int READ_TIMEOUT = 60000;

    private static Map<CertTypeEnum, PublicKey> yopPublicKeyMap;

    private static Map<CertTypeEnum, PrivateKey> isvPrivateKeyMap;

    private InternalConfig() {
        /*forbid instantiate*/
    }

    static {
        yopPublicKeyMap = Maps.newEnumMap(CertTypeEnum.class);
        isvPrivateKeyMap = Maps.newEnumMap(CertTypeEnum.class);

        try {
            // 允许在 VM arguments 中指定配置文件名 -Dyop.sdk.config.file=/yop_sdk_config_override.json
            SDKConfig config = load(System.getProperty(SP_SDK_CONFIG_FILE, DEFAULT_SDK_CONFIG_FILE));

            SERVER_ROOT = config.getServerRoot();

            APP_KEY = config.getAppKey();
            SECRET_KEY = config.getAesSecretKey();

            if (config.getConnectTimeout() != null && config.getConnectTimeout() >= 0) {
                CONNECT_TIMEOUT = config.getConnectTimeout();
            }
            if (config.getReadTimeout() != null && config.getReadTimeout() >= 0) {
                READ_TIMEOUT = config.getReadTimeout();
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
        } catch (Exception ex) {
            LOGGER.error("yop sdk load config file error", ex);
        }
    }

    static SDKConfig load(String configFile) {
        InputStream fis = null;
        SDKConfig config;
        try {
            fis = getInputStream(configFile);
            config = JsonUtils.loadFrom(fis, SDKConfig.class);
        } catch (Exception e) {
            System.out.println("Config format is error, file name:" + configFile);
            throw Exceptions.unchecked(e);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return config;
    }

    private static PublicKey loadPublicKey(CertConfig certConfig) {
        PublicKey publicKey;
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("Can't init YOP public key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    publicKey = RSAKeyUtils.string2PublicKey(certConfig.getValue());
                } catch (Exception e) {
                    System.out.println("Failed to load public key form config file is error, " + certConfig);
                    throw Exceptions.unchecked(e);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return publicKey;
    }

    private static PrivateKey loadPrivateKey(CertConfig certConfig) {
        PrivateKey privateKey = null;
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("Can't init ISV private key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    privateKey = RSAKeyUtils.string2PrivateKey(certConfig.getValue());
                } catch (Exception e) {
                    System.out.println("Failed to load private key form config file is error, " + certConfig);
                }
                break;
            case FILE_P12:
                try {
                    char[] password = certConfig.getPassword().toCharArray();
                    KeyStore keystore = KeyStore.getInstance(KeyStoreTypeEnum.PKCS12.getValue());
                    keystore.load(getInputStream(certConfig.getValue()), password);

                    Enumeration aliases = keystore.aliases();
                    String keyAlias = "";
                    while (aliases.hasMoreElements()) {
                        keyAlias = (String) aliases.nextElement();
                    }
                    privateKey = (PrivateKey) keystore.getKey(keyAlias, password);
                } catch (Exception e) {
                    System.out.println("Cert key is error, " + certConfig);
                    throw Exceptions.unchecked(e);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return privateKey;
    }

    public static InputStream getInputStream(String location) throws FileNotFoundException {
        InputStream fis;
        if (StringUtils.startsWith(location, "file://")) {
            fis = new FileInputStream(StringUtils.substring(location, 6));
        } else {
            fis = InternalConfig.class.getResourceAsStream(location);
        }
        return fis;
    }

    public static PublicKey getYopPublicKey(CertTypeEnum certType) {
        return yopPublicKeyMap.get(certType);
    }

    public static PrivateKey getISVPrivateKey(CertTypeEnum certType) {
        return isvPrivateKeyMap.get(certType);
    }
}
