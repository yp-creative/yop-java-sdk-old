package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopConfig;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.junit.Test;

/**
 * title: <br/>
 * description:描述<br/>
 * Copyright: Copyright (c)2016<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yp-tc-m-2645
 * @version 1.0.0ß
 * @since 16/9/12 下午6:58
 */
public class TestReport {
    @Test
    public void reportTest() {
        YopConfig.setAppKey("test");//yop应用
        YopConfig.setAesSecretKey("LVLDflZNINrrCFPIis9gCA==");//yop应用密钥，需要和短信通知应用的密钥保持一致才行，否则验证签名不通过
        YopConfig.setServerRoot("http://172.17.102.173:8064/yop-center");
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA-256");//具体看api签名算法而定
        request.setEncrypt(false);
        request.addParam("orderId", "51bfee75f620413b9398296603d8b883");
        YopResponse response = YopClient.post("/rest/v1.0/auth/getReport", request);
        System.out.println(response);
    }
}
