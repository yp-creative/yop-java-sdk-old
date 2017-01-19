package com.yeepay.g3.sdk.yop.utils;

import com.google.common.collect.Maps;
import com.yeepay.g3.facade.yop.ca.enums.CertTypeEnum;
import com.yeepay.g3.facade.yop.ca.enums.KeyStoreTypeEnum;
import com.yeepay.g3.frame.yop.ca.rsa.RSAKeyUtils;
import com.yeepay.g3.frame.yop.ca.utils.Exceptions;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.utils.config.CertConfig;
import com.yeepay.g3.sdk.yop.utils.config.SDKConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;
import java.util.Map;

/**
 * title: <br/>
 * description:描述<br/>
 * Copyright: Copyright (c)2011<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:48
 */
public final class InternalConfig {

    private static final String SP_SDK_CONFIG_FILE = "yop.sdk.config.file";

    private static final String DEFAULT_SDK_CONFIG_FILE = "/yop_sdk_config_default.json";
    private static final String DEFAULT_PROTOCOL_VERSION = "yop-auth-v2";
    private static final String DEFAULT_SDK_VERSION = "20170104.2103";

    private String protocolVersion;

    private String sdkVersion;

    private PublicKey isvPublicKey;

    private Map<CertTypeEnum, PublicKey> yopPublicKeyMap = Maps.newEnumMap(CertTypeEnum.class);

    private Map<CertTypeEnum, PrivateKey> isvPrivateKeyMap = Maps.newEnumMap(CertTypeEnum.class);

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public PublicKey getISVPublicKey() {
        return isvPublicKey;
    }

    public PublicKey getYopPublicKey(CertTypeEnum certType) {
        return yopPublicKeyMap.get(certType);
    }

    public PrivateKey getISVPrivateKey(CertTypeEnum certType) {
        return isvPrivateKeyMap.get(certType);
    }

    static InternalConfig load() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, NoSuchProviderException {
        // 允许在 VM arguments 中指定配置文件名 -Dyop.sdk.config.file=/yop_sdk_config_override.json
        String configFile = System.getProperty(SP_SDK_CONFIG_FILE);
        if (StringUtils.isBlank(configFile)) {
            configFile = DEFAULT_SDK_CONFIG_FILE;
        }

        SDKConfig config = load(configFile);
        InternalConfig internalConfig = new InternalConfig();
        if (StringUtils.isNotBlank(config.getDefaultProtocolVersion())) {
            internalConfig.protocolVersion = config.getDefaultProtocolVersion();
        } else {
            internalConfig.protocolVersion = DEFAULT_PROTOCOL_VERSION;
        }

        if (StringUtils.isNotBlank(config.getSdkVersion())) {
            internalConfig.sdkVersion = config.getSdkVersion();
        } else {
            internalConfig.sdkVersion = DEFAULT_SDK_VERSION;
        }

        if (null == config.getYopPublicKey()) {
            throw new YopClientException("Can't init YOP public key!");
        }
        if (config.getYopPublicKey().length > 0) {
            for (CertConfig certConfig : config.getYopPublicKey()) {
                internalConfig.yopPublicKeyMap.put(certConfig.getCertType(), loadPublicKey(certConfig));
            }
        }

        if (null != config.getIsvPrivateKey() && config.getIsvPrivateKey().length > 0) {
            for (CertConfig certConfig : config.getIsvPrivateKey()) {
                internalConfig.isvPrivateKeyMap.put(certConfig.getCertType(), loadPrivateKey(certConfig));
            }
        }
        return internalConfig;
    }

    static SDKConfig load(String configFile) {
        InputStream fis = null;
        SDKConfig config = null;
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
        PublicKey publicKey = null;
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
                    KeyStore keystore = null;
                    keystore = KeyStore.getInstance(KeyStoreTypeEnum.PKCS12.getValue());
                    keystore.load(InternalConfig.class.getResourceAsStream(certConfig.getValue()), password);
//                privateKey = (PrivateKey) keystore.getKey(certConfig.getCertType().getValue(), password);

                    // magic, amazing!
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

    private static InputStream getInputStream(String location) throws FileNotFoundException {
        InputStream fis = null;
        if (StringUtils.startsWith(location, "file://")) {
            fis = new FileInputStream(StringUtils.substring(location, 6));
        } else {
            fis = InternalConfig.class.getResourceAsStream(location);
        }
        return fis;
    }

    public static class Factory {

        private static final InternalConfig SINGELTON;

        static {
            InternalConfig config = null;
            try {
                config = InternalConfig.load();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException("Fatal: Failed to load the internal config for YOP Java SDK", ex);
            }
            SINGELTON = config;
        }

        public static InternalConfig getInternalConfig() {
            return SINGELTON;
        }
    }

}
