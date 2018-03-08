package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.frame.yop.ca.rsa.RSAKeyUtils;
import com.yeepay.g3.sdk.yop.YopServiceException;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

/**
 * title: 配置工具<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/9/18 17:08
 */
public class ConfigUtils {

    public static PublicKey loadPublicKey(CertConfig certConfig) {
        PublicKey publicKey;
        if (null == certConfig.getStoreType()) {
            throw new YopServiceException("Can't init YOP public key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    publicKey = RSAKeyUtils.string2PublicKey(certConfig.getValue());
                } catch (Exception ex) {
                    throw new YopServiceException(ex, "Failed to load public key form config file is error," + certConfig);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return publicKey;
    }

    public static PrivateKey loadPrivateKey(CertConfig certConfig) {
        PrivateKey privateKey = null;
        if (null == certConfig.getStoreType()) {
            throw new YopServiceException("Can't init ISV private key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    privateKey = RSAKeyUtils.string2PrivateKey(certConfig.getValue());
                } catch (Exception ex) {
                    throw new YopServiceException(ex, "Failed to load private key form config file is error, " + certConfig);
                }
                break;
            case FILE_P12:
                try {
                    char[] password = certConfig.getPassword().toCharArray();
                    KeyStore keystore = KeyStore.getInstance("PKCS12");
                    keystore.load(ConfigUtils.class.getResourceAsStream(certConfig.getValue()), password);

                    Enumeration aliases = keystore.aliases();
                    String keyAlias = "";
                    while (aliases.hasMoreElements()) {
                        keyAlias = (String) aliases.nextElement();
                    }
                    privateKey = (PrivateKey) keystore.getKey(keyAlias, password);
                } catch (Exception ex) {
                    throw new YopServiceException(ex, "Cert key is error, " + certConfig);
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
            fis = getResourceAsStream(location);
        }
        return fis;
    }

    public static InputStream getResourceAsStream(String resource) {
        if (StringUtils.startsWith(resource, "/")) {
            resource = StringUtils.substring(resource, 1);
        }
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);
        return in == null ? ConfigUtils.class.getResourceAsStream("/" + resource) : in;
    }

    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
