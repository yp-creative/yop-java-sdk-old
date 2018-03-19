package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.sdk.yop.utils.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * title: AppSDKConfig Support<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/2/8 15:57
 */
public class AppSDKConfigSupport {

    private static final Logger LOGGER = Logger.getLogger(AppSDKConfigSupport.class);

    private static final ConcurrentMap<String, Holder<AppSDKConfig>> APP_SDK_CONFIGS
            = new ConcurrentHashMap<String, Holder<AppSDKConfig>>();

    private static Holder<AppSDKConfig> defaultAppSDKConfig;

    private static volatile boolean inited = false;

    public static AppSDKConfig getConfig(String appKey) {
        init();
        Holder<AppSDKConfig> holder = APP_SDK_CONFIGS.get(formatAppKey(appKey));
        return holder == null ? null : holder.getValue();
    }

    public static AppSDKConfig getConfigWithDefault(String appKey) {
        init();
        Holder<AppSDKConfig> holder = APP_SDK_CONFIGS.get(formatAppKey(appKey));
        return holder == null ? getDefaultAppSDKConfig() : holder.getValue();
    }

    public static AppSDKConfig getDefaultAppSDKConfig() {
        init();
        return defaultAppSDKConfig.getValue();
    }

    private static String formatAppKey(String appKey) {
        return StringUtils.replace(appKey, ":", "");
    }

    private static void init() {
        if (inited) {
            return;
        }
        synchronized (AppSDKConfigSupport.class) {
            if (inited) {
                return;
            }
            for (Map.Entry<String, SDKConfig> entry : SDKConfigSupport.getSDKConfigs().entrySet()) {
                APP_SDK_CONFIGS.putIfAbsent(entry.getKey(), new Holder<AppSDKConfig>(new AppSDKConfigInitTask(entry.getValue())));
            }
            SDKConfig sdkConfig = SDKConfigSupport.getDefaultSDKConfig();
            if (SDKConfigSupport.isCustomDefault()) {
                defaultAppSDKConfig = APP_SDK_CONFIGS.get(sdkConfig.getAppKey());
            } else {
                defaultAppSDKConfig = new Holder<AppSDKConfig>(new AppSDKConfigInitTask(sdkConfig));
            }
            inited = true;
        }
    }

    private static class AppSDKConfigInitTask implements Callable<AppSDKConfig> {

        private final SDKConfig sdkConfig;

        AppSDKConfigInitTask(final SDKConfig sdkConfig) {
            this.sdkConfig = sdkConfig;
        }

        @Override
        public AppSDKConfig call() throws Exception {
            AppSDKConfig appSDKConfig = new AppSDKConfig();
            appSDKConfig.setAppKey(sdkConfig.getAppKey());
            appSDKConfig.setAesSecretKey(sdkConfig.getAesSecretKey());
            appSDKConfig.setServerRoot(sdkConfig.getServerRoot());
            if (sdkConfig.getYopPublicKey() != null && sdkConfig.getYopPublicKey().length >= 1) {
                appSDKConfig.storeYopPublicKey(sdkConfig.getYopPublicKey());
            }
            if (sdkConfig.getIsvPrivateKey() != null && sdkConfig.getIsvPrivateKey().length >= 1) {
                appSDKConfig.storeIsvPrivateKey(sdkConfig.getIsvPrivateKey());
            }
            return appSDKConfig;
        }
    }

}
