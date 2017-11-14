package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.junit.Test;

import java.io.IOException;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2016<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author yp-tc-m-2645
 * @version 1.0.0ß
 * @since 16/9/8 下午3:43
 */
public class TestPersonalBadInfo {
    @Test
    public void personnalBadInfoTest() throws IOException {
        YopRequest request = new YopRequest("test", "LVLDflZNINrrCFPIis9gCA==", "http://open.yeepay.com:8064/yop-center");
        request.setSignAlg("SHA-256");//具体看api签名算法而定
        request.setEncrypt(false);
        request.addParam("idcard", "360121198605075212");
        request.addParam("name", "万汉波");
        YopResponse response = YopClient.post("/rest/v1.0/auth/personal-bad-info-c", request);
        System.out.println(response);

    }
}
