package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.sdk.yop.utils.Holder;
import org.apache.log4j.Logger;

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

    private static volatile Holder<AppSDKConfig> defaultAppSDKConfig;

    private static volatile boolean inited = false;

    public static AppSDKConfig getConfig(String appKey) {
        init();
        Holder<AppSDKConfig> holder = APP_SDK_CONFIGS.get(appKey);
        return holder == null ? null : holder.getValue();
    }

    public static AppSDKConfig getDefaultAppSDKConfig() {
        init();
        return defaultAppSDKConfig == null ? null : defaultAppSDKConfig.getValue();
    }

    private static void init() {
        if (inited) {
            return;
        }
        synchronized (AppSDKConfigSupport.class) {
            if (inited) {
                return;
            }
            for (SDKConfig sdkConfig : SDKConfigSupport.getSDKConfigs().values()) {
                APP_SDK_CONFIGS.putIfAbsent(sdkConfig.getAppKey(), new Holder<AppSDKConfig>(new AppSDKConfigInitTask(sdkConfig)));
            }
            SDKConfig defaultSDKConfig = SDKConfigSupport.getDefaultConfig();
            if (defaultSDKConfig != null) {
                defaultAppSDKConfig = new Holder<AppSDKConfig>(new AppSDKConfigInitTask(defaultSDKConfig));
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
            appSDKConfig.setYopPublicKey(ConfigUtils.loadPublicKey(sdkConfig.getYopPublicKey()[0]));
            appSDKConfig.setIsvPrivateKey(ConfigUtils.loadPrivateKey(sdkConfig.getIsvPrivateKey()[0]));
            return appSDKConfig;
        }
    }

}
