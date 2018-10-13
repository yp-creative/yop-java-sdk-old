package com.yeepay.g3.sdk.yop.config.support;

import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.exception.config.MissingConfigException;
import org.apache.commons.lang3.StringUtils;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 15:48
 */
public class CheckUtils {

    public static void checkCustomSDKConfig(SDKConfig sdkConfig) {
        if (StringUtils.isEmpty(sdkConfig.getAppKey())) {
            throw new MissingConfigException("appKey", "appKey is empty");
        }
    }
}
