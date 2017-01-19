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
 * @since 16/9/8 下午3:43
 */
public class TestPersonalBadInfo {
    @Test
    public void personnalBadInfoTest() {
        YopConfig.setAppKey("test");//yop应用
        YopConfig.setAesSecretKey("LVLDflZNINrrCFPIis9gCA==");//yop应用密钥，需要和短信通知应用的密钥保持一致才行，否则验证签名不通过
        YopConfig.setServerRoot("http://172.17.102.173:8064/yop-center");
//        YopConfig.setAppKey("highpay");
//        YopConfig.setAesSecretKey("fdcHMUoeM37pRzyJV7AuwA==");
//        YopConfig.setServerRoot("http://10.151.30.87:8064/yop-center");
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA-256");//具体看api签名算法而定
        request.setEncrypt(false);
//        request.addParam("appId","1b032989-6271-4603-960f-6e0d003143f4");
//        request.addParam("tokenId","76760ad6-8492-43d1-8823-24269d48c286");
        request.addParam("idcard", "360121198605075212");
        request.addParam("name", "万汉波");
        YopResponse response = YopClient.post("/rest/v1.0/auth/personal-bad-info-c", request);
        System.out.println(response);

    }
}
