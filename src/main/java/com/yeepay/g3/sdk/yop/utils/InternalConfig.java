package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.YopServiceException;
import com.yeepay.g3.sdk.yop.config.*;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import org.apache.log4j.Logger;

import java.security.PrivateKey;
import java.security.PublicKey;

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

    public static final String PROTOCOL_VERSION = "yop-auth-v2";

    public static int CONNECT_TIMEOUT = 30000;
    public static int READ_TIMEOUT = 60000;

    public static int MAX_CONN_TOTAL = 200;
    public static int MAX_CONN_PER_ROUTE = 100;

    public static boolean TRUST_ALL_CERTS = false;

    static {
        init();
    }

    private static void init() {
        SDKConfig sdkConfig = SDKConfigSupport.getDefaultSDKConfig();
        if (sdkConfig != null && sdkConfig.getHttpClient() != null) {
            HttpClientConfig clientConfig = sdkConfig.getHttpClient();
            if (clientConfig.getConnectTimeout() != null) {
                CONNECT_TIMEOUT = clientConfig.getConnectTimeout();
            }
            if (clientConfig.getReadTimeout() != null) {
                READ_TIMEOUT = clientConfig.getReadTimeout();
            }
            if (clientConfig.getMaxConnTotal() != null) {
                MAX_CONN_TOTAL = clientConfig.getMaxConnTotal();
            }
            if (clientConfig.getMaxConnPerRoute() != null) {
                MAX_CONN_PER_ROUTE = clientConfig.getMaxConnPerRoute();
            }
        }
    }

    public static PublicKey getYopPublicKey(CertTypeEnum certType) {
        AppSDKConfig defaultAppSDKConfig = AppSDKConfigSupport.getDefaultAppSDKConfig();
        return defaultAppSDKConfig.loadYopPublicKey(certType);
    }

    public static PrivateKey getISVPrivateKey(String appKey, CertTypeEnum certType) {
        AppSDKConfig appSDKConfig = AppSDKConfigSupport.getConfig(appKey);
        if (appSDKConfig == null) {
            throw new YopServiceException("SDKConfig for appKey:" + appKey + " not exist.");
        }
        return appSDKConfig.loadPrivateKey(certType);
    }

    public static PrivateKey getISVPrivateKey(CertTypeEnum certType) {
        AppSDKConfig defaultAppSDKConfig = AppSDKConfigSupport.getDefaultAppSDKConfig();
        return defaultAppSDKConfig.loadPrivateKey(certType);
    }

}
