package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.sdk.yop.config.provider.DefaultFileAppSdkConfigProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/6/12 15:57
 */
public class SDKConfigReadTest {

    @Test
    public void test() {
        System.setProperty("yop.sdk.config.dir", "multiconfig");
        DefaultFileAppSdkConfigProvider provider = new DefaultFileAppSdkConfigProvider();
        Assert.assertNotNull(provider.getConfig("app_oEdCdceLQF63hvYz"));
    }
}