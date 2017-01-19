package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

/**
 * title: <br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 15/7/8 10:23
 */
public class LocalDemo {

    private static final String BASE_URL = "http://127.0.0.1:8064/yop-center/";

    private static final String[] APP_KEYS = {"yop-boss", "jinkela2", "laike"};
    private static final String[] APP_SECRETS = {"cGB2CeC3YmwSWGoVz0kAvQ==", "0/ZoyfKku0tunPunw7dbfA==", "yHCV/A1cR1ybaMmvqYk7yw=="};

    @Test
    public void testIdCard() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[i], BASE_URL);
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("appKey", APP_KEYS[i]);
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186692");
        request.addParam("testDate", "2012-10-09 12:13:14");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        System.out.println(response.toString());
    }

    @Test
    public void testEnterprise() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[i], BASE_URL);
        request.setEncrypt(false);
        request.setSignRet(true);
        request.addParam("appKey", APP_KEYS[i]);//这个写YOP就可以了
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "wenkang.zhang");//请求者标识
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.2/auth/enterprise", request);
        System.out.println(response.toString());
    }

    @Test
    public void testWhiteList() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[i], BASE_URL);
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("appKey", APP_KEYS[i]);
        request.setSignAlg("sha-256");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186691");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        System.out.println(response.toString());
    }

    @Test
    public void testCreateToken() {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[i], "http://172.17.102.177:7777/yop-center/");
        request.setEncrypt(true);
        request.setSignRet(true);
//        request.setSignAlg("SHA1");
//        request.addParam("customerNo", "10040011444");
        request.addParam("appKey", APP_KEYS[i]);

        request.addParam("grant_type", "password");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "wenkang.zhang");
        request.addParam("scope", "test");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        System.out.println(response.toString());
    }

    @Test
    public void testLoadClass() throws Exception {
        int i = 0;
//        YopRequest request = new YopRequest(null, APP_SECRETS[i], BASE_URL);
        YopRequest request = new YopRequest(null, APP_SECRETS[i], "http://172.17.102.173:8064/yop-center/");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("appKey", APP_KEYS[i]);//这个写YOP就可以了
        request.addParam("className", "com.yeepay.g3.facade.ymf.facade.laike.OrderFacade");//这个写YOP就可以了
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        System.out.println(response.toString());
    }


    @Test
    public void testLaike() {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i], BASE_URL);
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("grant_type", "password");
        request.addParam("refresh_token", "123");//请求流水标识
        request.addParam("scope", "123");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/laike/token", request);
        System.out.println(response.toString());
    }

    @Test
    public void testLaike2() {
        int i = 2;
        YopRequest request = new YopRequest(null, APP_SECRETS[i], BASE_URL);
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("appKey", APP_KEYS[i]);
        request.addParam("member_no", "2");// 会员号-null, default:
        request.addParam("merchant_account", "12");// 商编-null, default: YEEPAY
        request.addParam("request_no", "123");// 交易系统支付请求唯一流水号-null, default:
        request.addParam("confusion", "12");// PC 收银台加密订单号-null, default:
        request.addParam("cashiers", "44");// 收银台编号-null, default:
        request.addParam("product_name", "x");// 商品名称-null, default:
        request.addParam("pay_org_type", "NOCARD");// 支付来源-null, default: NOCARD
        request.addParam("identity_type", "USER_ID");// 会员号类型-null, default: USER_ID
        request.addParam("user_org", "12");// 用户来源-null, default:
        request.addParam("bind_id", "12");// 绑卡 ID（可选）-null, default:

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/laike/user/pwd/find/sms/confirm", request);
        System.out.println(response.toString());
    }

    @Test
    public void testBasicAuth() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[0], BASE_URL);
        request.addParam("appKey", APP_KEYS[i]);//这个写YOP就可以了
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "wenkang.zhang");//请求者标识
        System.out.println(request.toQueryString());
        YopResponse response = YopClient2.postBasic("/rest/v2.2/auth/enterprise", request);
        System.out.println(response.toString());
    }

    @Test
    public void testRsaAuth() throws Exception {
        String secretKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+sfPHdt9C0Rzoy0IGC5wwEMfQQ8bto9WwdMx/VZacIlcpVLdwCp8LqjZTGi4GnFqYJr+Jn+lmSHCq1stACc4K0GH9L+a02LnZQIdzPQS9tq5ObYhWOBXn6P0I0Xj04uwUf1WXHIl6F3GNtnnKdLcYKzSkGC9CCwuuiVzkS8wRj12nuPPcNI7Y4/yXs3AXGs9FUCdzcj2bPMvTIRbER7SurDPiMzv+oEMkl2FpzHb4YHKMMk4lsaoW3uOW9k4dkYzfRz3Q0qe4d1nn5ApJAKkivph5puRL3TDwEosWKl5NNVICw7KWJ5mcWMiEGCNI3v9HF+9S8lkznUFQwS6rcuhHAgMBAAECggEAIV/nuIs3e+w5UQzx2zkA9vCY5/xUvpaj+aQXflmuaFkiRBuNl1BkkZgSteypDYMj3+k4MJAKO1qGTYAWRnjw1Levzq4phJFBxaIjkqmlQMbOLu/AosZiHxqJ3pc6wjFM/DFk+3OFJBciSpkbK7HNsa8uIWISM07Xlo538YTKjG/rd9cyPqBRrfJKjACLjwujFxhx8VFBavPrS8P1dEnyfkV+MHAjk0iX08hs4JtGC4AWgVlD3R7CJZ2ETR98XO8tXhfLwafuo5cafqfl0Pp0BKFLSyM4Y6dN44tbu/2U4B1an2OEAhSuonbG80L8QBtbbzNrR314SJxPuHRHccwOQQKBgQDuHIGcEL5iliOChbOhxYr8/FIVMsTzGb4Ex+pRdUots2nvTmHzOKRR0qHsyK10xNuvj4zf/z7S7lyXz/AxHQ4x3HWmGH7BFOYli9WckYvfRESiUt2TdAToK/8Lel8dIuSgeOEuapeLqRuowVKAA89I1maEsENX+DBBevv6RyVglwKBgQDNBYKOb5VprU3KUr6wSUbsmvVjaQDK7N1nqLbPewRW5rnBB7xN1JO7emU3AXJsPoqQg4DYM7E3Y8vFtvV5hxAXzVxgSqieHM9tosEmOzjcozZWHS1+k+mRxmoF++pNq7dYSlEkp0jLxVG3IMgPIsm676tsn7iEjk48ezMPvf370QKBgAfZ9sgfoTd8/eTC5d5ytheLdmVujCH7+lEMCoTvGigRn2+dpQsxFfIAwQgeweF00E2krR+Buh+YboMu/xuPerVYMKTub9eMCd65Eq0b+4G8LYswsk+3ZqYFNV66hUJfsSw9cCdoqmCXjertnPvNpFshfrXnXgEblPPDy2VATVI3AoGBAK6lOfDBpsJtqa1mzR94XF1jJxO2t8ElAvmxY4Dcpvar9qIzR7n5EpuKiLvRlLoWkIzIKLKDXjvz2/qhfvw2DIRqIH26O2bs3dnj4asCmc6BjeDPXBjOQRI3tctBK/dcBoPEUtjpU36ZqKFZ4HHcTYHbdejDKjhozMaCoB2R19BxAoGANZkaMuYjnmD8ZtswTnv0CfrPghJ3TBqmsNjEOIq40ykl82c1tPbI8HceL7vPrGBvagl8gsw3DkF/K+RiGYIiq40+O9k2VaurWXIbx29U5WaT8v8Aaf5tABXCnKluBK69ohBwQPSZ57peAvRQSH68PsI7pJlX2G7moBt8AYvXLdU=";
        String BASE_URL = "http://10.151.30.80:18064/yop-center/";
        String appKey = "OPR:10040007799-25";
        YopRequest request = new YopRequest(appKey, secretKey, BASE_URL);
        request.addParam("customerNo", "10040007799");
        request.addParam("parentCustomerNo", "10040007799");
        request.addParam("requestId", "requestId1480392119078");
        request.addParam("uniqueOrderNo", "1001201611290000000000000808");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient3.postRsa("/rest/v2.0/opr/queryorder", request);
        System.out.println(response.toString());
    }
}
