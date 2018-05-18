package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

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
public class FileSdkConfigQaDemo {

    //DefaultCachedAppSdkprovider
    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("yop.sdk.config.dir", "file:///Users/yp-tc-m2552/Desktop/multiconfig");
//        System.setProperty("yop.sdk.config.file", "yop_sdk_config_app1.json,yop_sdk_config_app2.json");
    }


    @Test
    public void test() throws IOException, InterruptedException {
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
