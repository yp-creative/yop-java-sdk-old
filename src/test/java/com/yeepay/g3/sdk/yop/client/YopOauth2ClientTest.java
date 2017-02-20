package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.client.oauth2.YopOauth2Client;
import com.yeepay.g3.sdk.yop.client.oauth2.YopOauth2Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * title: <br/>
 * description:描述<br/>
 * Copyright: Copyright (c)2011<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/1/24 上午11:54
 */
public class YopOauth2ClientTest {

    static final String BASE_URL = "http://172.17.103.170:8064/yop-center/";
    static final String APP_KEY = "yop-boss";
    static final String ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJodHRwczovL29wZW4ueWVlcGF5LmNvbSIsImNsaSI6InlvcC1ib3NzIiwiY2lkIjoiMjEiLCJ1aWQiOiJzaXFpIiwic2NvcGUiOlsidGVzdCJdLCJhdF9leHBfaW4iOjQzMjAwLCJydF9leHBfaW4iOjI1OTIwMDAsInN1YiI6ImFjY2Vzc190b2tlbiIsIm5vbmNlIjoiQVQ6eW9wLWJvc3M6MjE6c2lxaTpjMTY5YTg4ZC04NGQ2LTRmZGQtODFhZi0yNzUyNGY0MmE1MjgiLCJleHAiOjE0ODc2MTYyMTZ9.Qm_TcXnjSN5eUt_0i97XYyAXzWypgDku95hhY7yqELNJdpZQEXQcDFvENXOvlL99jjMZmUKaPNv_JykRSMeVPw";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        YopConfig.setServerRoot(BASE_URL);
    }

    @Before
    public void setUp() throws Exception {
        // TODO 请求 oauth2 子系统
        // ACCESS_TOKEN =
    }

    @Test
    public void testRichRequest() throws Exception {
        YopOauth2Request request = new YopOauth2Request(APP_KEY, ACCESS_TOKEN);

        request.addParam("corpName", "易宝支付有限公司");
        request.addParam("latency", "200");
        request.addParam("regNo", "630104063035716");
        request.addParam("requestCustomerId", "yop-boss");
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));

        String methodOrUri = "/rest/v2.2/auth/enterprise";
        YopResponse response = YopOauth2Client.postOauth2(methodOrUri, request);
        assertEquals("SUCCESS", response.getState());
    }

}
