package com.yeepay.g3.sdk.yop.config;

import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * title: 应用SDKConfig<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/2/8 15:32
 */
public class AppSDKConfig implements Serializable {

    private static final long serialVersionUID = 5717760813596644163L;

    private String appKey;

    private String serverRoot;

    private String aesSecretKey;

    private PublicKey yopPublicKey;

    private PrivateKey isvPrivateKey;

    private Map<CertTypeEnum, PublicKey> yopPublicKeys;

    private Map<CertTypeEnum, PrivateKey> isvPrivateKeys;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public AppSDKConfig withAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public AppSDKConfig withServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
        return this;
    }

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public AppSDKConfig withAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
        return this;
    }

    public PublicKey getYopPublicKey() {
        return yopPublicKey;
    }

    public void setYopPublicKey(PublicKey yopPublicKey) {
        this.yopPublicKey = yopPublicKey;
    }

    public AppSDKConfig withYopPublicKey(PublicKey yopPublicKey) {
        this.yopPublicKey = yopPublicKey;
        return this;
    }

    public PrivateKey getIsvPrivateKey() {
        return isvPrivateKey;
    }

    public void setIsvPrivateKey(PrivateKey isvPrivateKey) {
        this.isvPrivateKey = isvPrivateKey;
    }

    public AppSDKConfig withIsvPrivateKey(PrivateKey isvPrivateKey) {
        this.isvPrivateKey = isvPrivateKey;
        return this;
    }

    public void storeYopPublicKey(CertConfig[] yopPublicKeys) {
        this.yopPublicKey = ConfigUtils.loadPublicKey(yopPublicKeys[0]);
        this.yopPublicKeys = Maps.newHashMap();
        this.yopPublicKeys.put(yopPublicKeys[0].getCertType(), this.yopPublicKey);
        for (int i = 1; i < yopPublicKeys.length; i++) {
            this.yopPublicKeys.put(yopPublicKeys[i].getCertType(), ConfigUtils.loadPublicKey(yopPublicKeys[i]));
        }
    }

    public void storeIsvPrivateKey(CertConfig[] isvPrivateKeys) {
        this.isvPrivateKey = ConfigUtils.loadPrivateKey(isvPrivateKeys[0]);
        this.isvPrivateKeys = Maps.newHashMap();
        this.isvPrivateKeys.put(isvPrivateKeys[0].getCertType(), this.isvPrivateKey);
        for (int i = 1; i < isvPrivateKeys.length; i++) {
            this.isvPrivateKeys.put(isvPrivateKeys[i].getCertType(), ConfigUtils.loadPrivateKey(isvPrivateKeys[i]));
        }
    }

    public PublicKey loadYopPublicKey(CertTypeEnum certType) {
        return this.yopPublicKeys.get(certType);
    }

    public PrivateKey loadPrivateKey(CertTypeEnum certType) {
        return this.isvPrivateKeys.get(certType);
    }
}
