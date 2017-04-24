package com.yeepay.g3.sdk.yop.client;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/1/24 上午11:54
 */
public class YopBaseClientTest {

    @Test
    public void testRichRequest() throws Exception {
        YopRequest request = new YopRequest("appKey", "appSecret");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张三");
        request.addParam("idCardNumber", "370982199101186691111");

        String methodOrUri = "/rest/v1.2/auth/idcard";
        String serverUrl = AbstractClient.richRequest(methodOrUri, request);
        assertEquals(YopConfig.getServerRoot() + "/rest/v1.2/auth/idcard", serverUrl);
    }

}
