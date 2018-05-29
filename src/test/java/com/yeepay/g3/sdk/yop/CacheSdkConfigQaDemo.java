package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.config.provider.BaseCachedAppSdkConfigProvider;
import com.yeepay.g3.sdk.yop.config.support.SDKConfigUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/18 15:04
 */
public class CacheSdkConfigQaDemo {

    //CustomCachedAppSdkProvider
    @BeforeClass
    public static void setUp() throws Exception {
        AppSdkConfigProviderRegistry.registerCustomProvider(new MockCacheAppSdkConfigProvider("yop-boss", 30L, TimeUnit.SECONDS));
    }


    static class MockCacheAppSdkConfigProvider extends BaseCachedAppSdkConfigProvider {

        public MockCacheAppSdkConfigProvider(Long expire, TimeUnit timeUnit) {
            super(expire, timeUnit);
        }

        public MockCacheAppSdkConfigProvider(String defaultAppKey) {
            super(defaultAppKey);
        }

        public MockCacheAppSdkConfigProvider(String defaultAppKey, Long expire, TimeUnit timeUnit) {
            super(defaultAppKey, expire, timeUnit);
        }

        @Override
        protected SDKConfig loadSDKConfig(String appKey) {
            logger.info("try to load..........." + appKey);
            if (StringUtils.equals(appKey, "app_oEdCdceLQF63hvYz")) {
                return SDKConfigUtils.loadConfig("/multiconfig/yop_sdk_config_app1.json");
            } else if (StringUtils.equals(appKey, "app_eyMpU7WNTxPW464B")) {
                return SDKConfigUtils.loadConfig("/multiconfig/yop_sdk_config_app2.json");
            }
            return null;
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {

        AssertUtils.assertYopResponse(YopClient3.postRsa("/rest/v3.0/auth/enterprise", generateRequest("app_oEdCdceLQF63hvYz")));
        AssertUtils.assertYopResponse(YopClient3.postRsa("/rest/v3.0/auth/enterprise", generateRequest("app_eyMpU7WNTxPW464B")));

        //让缓存失效
        Thread.sleep(40000L);

        AssertUtils.assertYopResponse(YopClient3.postRsa("/rest/v3.0/auth/enterprise", generateRequest("app_oEdCdceLQF63hvYz")));
        AssertUtils.assertYopResponse(YopClient3.postRsa("/rest/v3.0/auth/enterprise", generateRequest("app_eyMpU7WNTxPW464B")));


    }

    private YopRequest generateRequest(String appKey) {
        YopRequest request = new YopRequest(appKey);
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识
        return request;

    }


}
