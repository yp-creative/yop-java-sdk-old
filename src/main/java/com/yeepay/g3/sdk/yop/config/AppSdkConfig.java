package com.yeepay.g3.sdk.yop.config;

import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.config.support.ConfigUtils;
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
public class AppSdkConfig implements Serializable {

    private static final long serialVersionUID = 5717760813596644163L;

    private String appKey;

    private String serverRoot;

    private String aesSecretKey;

    private PublicKey defaultYopPublicKey;

    private PrivateKey defaultIsvPrivateKey;

    private HttpClientConfig httpClientConfig;

    private Map<CertTypeEnum, PublicKey> yopPublicKeys;

    private Map<CertTypeEnum, PrivateKey> isvPrivateKeys;

    private ProxyConfig proxy;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public AppSdkConfig withAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public AppSdkConfig withServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
        return this;
    }

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public AppSdkConfig withAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
        return this;
    }

    public PublicKey getDefaultYopPublicKey() {
        return defaultYopPublicKey;
    }

    public void setDefaultYopPublicKey(PublicKey defaultYopPublicKey) {
        this.defaultYopPublicKey = defaultYopPublicKey;
    }

    public PrivateKey getDefaultIsvPrivateKey() {
        return defaultIsvPrivateKey;
    }

    public void setDefaultIsvPrivateKey(PrivateKey defaultIsvPrivateKey) {
        this.defaultIsvPrivateKey = defaultIsvPrivateKey;
    }

    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    public void storeYopPublicKey(CertConfig[] yopPublicKeys) {
        this.defaultYopPublicKey = ConfigUtils.loadPublicKey(yopPublicKeys[0]);
        this.yopPublicKeys = Maps.newHashMap();
        this.yopPublicKeys.put(yopPublicKeys[0].getCertType(), this.defaultYopPublicKey);
        for (int i = 1; i < yopPublicKeys.length; i++) {
            this.yopPublicKeys.put(yopPublicKeys[i].getCertType(), ConfigUtils.loadPublicKey(yopPublicKeys[i]));
        }
    }

    public void storeIsvPrivateKey(CertConfig[] isvPrivateKeys) {
        this.defaultIsvPrivateKey = ConfigUtils.loadPrivateKey(isvPrivateKeys[0]);
        this.isvPrivateKeys = Maps.newHashMap();
        this.isvPrivateKeys.put(isvPrivateKeys[0].getCertType(), this.defaultIsvPrivateKey);
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

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public static final class Builder {
        private SDKConfig sdkConfig;

        public Builder() {
        }

        public static Builder anAppSdkConfig() {
            return new Builder();
        }

        public Builder withSDKConfig(SDKConfig sdkConfig) {
            this.sdkConfig = sdkConfig;
            return this;
        }

        public AppSdkConfig build() {
            AppSdkConfig appSdkConfig = new AppSdkConfig();
            appSdkConfig.setAppKey(sdkConfig.getAppKey());
            appSdkConfig.setAesSecretKey(sdkConfig.getAesSecretKey());
            appSdkConfig.setServerRoot(sdkConfig.getServerRoot());

            if (sdkConfig.getYopPublicKey() != null && sdkConfig.getYopPublicKey().length >= 1) {
                appSdkConfig.storeYopPublicKey(sdkConfig.getYopPublicKey());
            }
            if (sdkConfig.getIsvPrivateKey() != null && sdkConfig.getIsvPrivateKey().length >= 1) {
                appSdkConfig.storeIsvPrivateKey(sdkConfig.getIsvPrivateKey());
            }
            appSdkConfig.setHttpClientConfig(sdkConfig.getHttpClient());
            appSdkConfig.setProxy(sdkConfig.getProxy());
            return appSdkConfig;
        }
    }
}
