package com.yeepay.g3.sdk.yop;

import com.TrustAllHttpsCertificates;
import com.yeepay.g3.sdk.yop.client.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
public class ProductiveDemo {

    private static final String[] APP_KEYS = {"yop-boss", "jinkela", "k1242692364"};
    private static final String[] APP_SECRETS = {"NWNZQslh36xJP2a5rodX1Q==", "T7aoGzBJrPDCL3qoo1ij/g==", "/7tTqP3OJr6A6j3mAUEFoQ=="};

    @BeforeClass
    public static void setUp() throws Exception {
        TrustAllHttpsCertificates.setTrue();

        final String BASE_URL = "https://open.yeepay.com/yop-center/";
        YopConfig.setServerRoot(BASE_URL);
    }

    @Test
    public void testIdCard() throws Exception {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.setEncrypt(true);
        request.setSignRet(true);
//        request.setSignAlg("sha-256");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186691111");

        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testQueryMemberAccount() throws Exception {
        YopRequest request = new YopRequest(null,
                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "0");
        request.addParam("platformUserNo", "1234567890123456789012345673333");

        YopResponse response = YopClient.post("/rest/v1.0/member/queryAccount", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void v() {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("customerNo", "10040011444");
        request.addParam("merchantNo", "10040028626");
//        request.addParam("platformUserNo", "12345678901234567890123456789012");

        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryBalancesss", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testEnterprise() throws Exception {
        YopRequest request = new YopRequest("yop-boss", "QFdODaBYBiVuLpP+sbyH+g==");
        request.setEncrypt(false);
        request.setSignRet(true);
        request.addParam("appKey", "yop-boss");//这个写YOP就可以了
//        request.addParam("requestSystem", "YOP");//这个写YOP就可以了
        request.addParam("corpName", "青海韩都忆餐饮管理有限公司");//企业名称
        request.addParam("regNo", "630104063037404");//工商注册号
        request.addParam("requestCustomerId", "jinkela");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "wenkang.zhang");//请求者标识

        YopResponse response = YopClient.post("/rest/v1.2/auth/authenterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testName1() throws Exception {
        YopRequest request = new YopRequest(null,
                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "YOP-SDK-" + System.currentTimeMillis());
//		request.addParam("platformUserNo","YOP-USERNO-" + System.currentTimeMillis());
        request.addParam("platformUserNo", "8880222");
//		request.addParam("platformUserNo","YOP-USERNO-1435560994654");

        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryAccount", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testName2() throws Exception {
        YopRequest request = new YopRequest(null,
                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "YOP-SDK-" + System.currentTimeMillis());
        request.addParam("platformUserNo", "20150623143151652niuniu");
        request.addParam("orderNo", "YOP-SDK-ORDER-" + System.currentTimeMillis());
        request.addParam("amount", "20");
        request.addParam("payproducttype", "NET");
        request.addParam("bankid", "");
        request.addParam("callbackurl", "http://50.1.1.24:8018/fundtrans-hessian/");
        request.addParam("webcallbackurl", "http://localhost:8080/reciver/page");

        String requestUri = YopClient.buildURL("/rest/v1.0/member/gatewayDeposit", request);
        System.out.println(requestUri);

        YopResponse response = YopClient.get("/rest/v1.0/member/gatewayDeposit", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void test1() {
        YopRequest request = new YopRequest(null,
                "s5KI8r0920SQ339oVlFE6eWJ0yk019SD7015nw39iaXJp10856z0C1d7JV5l");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.addParam("customerNo", "10011830665");
        request.addParam("customernumber", "10012544672");
        request.addParam("requestid", System.currentTimeMillis());
        request.addParam("amount", "0.1");
        request.addParam("callbackurl", "http://www.baidu.com");
        request.addParam("webcallbackurl", "http://www.baidu.com");
        request.addParam("bankid", "ICBC");
        request.addParam("payproducttype", "SALES");

        YopResponse response = YopClient.post("/rest/v1.0/merchant/pay", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test//发送短信接口
    public void testSendSms() {
        YopConfig.setAppKey("TestAppKey002");//yop应用
        YopConfig.setAesSecretKey("TestAppSecret002");//yop应用密钥，需要和短信通知应用的密钥保持一致才行，否则验证签名不通过
        YopRequest request = new YopRequest();
        // request.setSignAlg("SHA1");
        request.setSignAlg("MD5");//具体看api签名算法而定
        //request.setEncrypt(true);
        String notifyRule = "fundauth_MOBILE_IFVerify";//通知规则
        List recipients = new ArrayList();//接收人
        recipients.add(0, "18253166342");
        String content = "{code:12345,something:something}";//json字符串，code，mctName为消息模板变量
        String extNum = "01";//扩展码
        String feeSubject = "0.01";//计费主体
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);
        request.addParam("extNum", extNum);
        request.addParam("feeSubject", feeSubject);

        YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testSendSmsQa() {
        YopConfig.setAppKey("openSmsApi");//yop应用
        YopConfig.setAesSecretKey("1234554321");//yop应用密钥，需要和短信通知应用的密钥保持一致才行，否则验证签名不通过
        YopRequest request = new YopRequest();
        request.setSignAlg("MD5");//具体看api签名算法而定
        //request.setEncrypt(true);
        String notifyRule = "商户结算短信通知";//通知规则
        List recipients = new ArrayList();//接收人
        recipients.add(0, "18253166342");
        String content = "{code:1235}";//json字符串，code，mctName为消息模板变量
        String extNum = "3";//扩展码
        String feeSubject = "0.01";//计费主体
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);
        request.addParam("extNum", extNum);
        request.addParam("feeSubject", feeSubject);

        YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testSendSmsProduct() {
        YopConfig.setAppKey("ypo2o");//yop应用
        YopConfig.setAesSecretKey("tpcY6k2RSpEod7hsJIp33Q==");//yop应用密钥，需要和短信通知应用的密钥保持一致才行，否则验证签名不通过
        YopRequest request = new YopRequest();
        request.setSignAlg("MD5");//具体看api签名算法而定
        // request.setEncrypt(true);
        String notifyRule = "EGOU_VERIFY";//通知规则
        List recipients = new ArrayList();//接收人
        recipients.add(0, "18519193582");
        String content = "{message1:123445}";//json字符串，code，mctName为消息模板变量
        String extNum = "52";//扩展码
        String feeSubject = "0.01";//计费主体
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);
        request.addParam("extNum", extNum);
        request.addParam("feeSubject", feeSubject);

        YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
        AssertUtils.assertYopResponse(response);
    }


    @Test
    public void testValidate() {
        YopRequest request = new YopRequest(null,
                "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setEncrypt(false);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("appKey", "yop-boss");
        request.addParam("not_null", "10011830665");
        request.addParam("complex", "33647");
        request.addParam("not_blank", "张文康");

        request.addParam("length", "fkdjfld");
        request.addParam("range", "10");
        request.addParam("int", "3");
        request.addParam("email", "wenkang.zhang@yeepay.com");
        request.addParam("mobile", "15901189967");
        request.addParam("idcard", "370982199101186");

        YopResponse response = YopClient.get("/rest/v1.0/kong/validator", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testWhiteList() throws Exception {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setEncrypt(false);
        request.setSignRet(true);
        request.addParam("appKey", "yop-boss");
        request.setSignAlg("sha-256");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186691");

        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testCreateToken() {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setEncrypt(true);
        request.setSignRet(true);
//        request.setSignAlg("SHA1");
//        request.addParam("customerNo", "10040011444");
        request.addParam("appKey", "yop-boss");

        request.addParam("grant_type", "password");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "wenkang.zhang");
        request.addParam("scope", "test");

        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAmount() {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setEncrypt(true);
        request.setSignRet(true);
//        request.setSignAlg("SHA1");
        request.addParam("customerNo", "10040011444");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康", true);
        request.addParam("idCardNumber", "370982199101186691");
        request.addParam("bankCardNumber", "4392250043179877");

        YopResponse response = YopClient.post("/rest/v2.0/auth/debit3", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testJvmCollect() {
        int i = 0;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.setEncrypt(false);
        request.setSignRet(true);
        request.setSignAlg("sha-256");

        YopResponse response = YopClient.post("/rest/v1.0/system/jvm", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLaike() {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.setEncrypt(false);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("appKey", APP_KEYS[0]);
        request.addParam("request_no", RandomStringUtils.randomAlphanumeric(20));
        request.addParam("phone_no", "13522666106");
        request.addParam("location", "test");
        request.addParam("version_id", "1");
        request.addParam("pwd", "123qwe");
        request.addParam("imei", APP_KEYS[0]);

        YopResponse response = YopClient.post("/rest/v1.0/laike/login", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLaikeToken() {
        int i = 0;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("grant_type", "password");
        request.addParam("refresh_token", "123");//请求流水标识
        request.addParam("scope", "123");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/laike/token", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void yop() {
        //int i = 0;
//        String secretKey ="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEk5fANXccim3575EasuANg_U_rM14xSNKi7KWdHRSY7hAJK7N-QhWOGz81rKLmgyd4Q7c1dmZbLzNnOQlGYjBAy2jH0a84EW5dM3GqNCX_A3iJda8xSUIpsaxOceqc46z370sijjVmn9rlbJMNpx-BZuLAHivYVOtg95-VOeWiyGMsDlMpAZQjD-bRWshMV41Bzlq-9u5h-NGJ7371wvrqSpAId2Jxp9OR-G8_nSByAvS3y2xliwbntK9MEcD64ew-6dkLb3hsmVc2pWm0uZPumnirjyvGatd--OrEAtb_8-Bki-ukqJWOAdb5bdp29gkg738Gdl-5at3e6uHdz0JAgMBAAECggEAYYJUmLA6PSmrnaqQJPzvQcGOfhjQv0TvogKBhZt9eqORfsv8Lc4-TXwO3R_kDj1tjilbzx0SgH-zld8RBiBzrtJxnIqCcqTZY3__YWAEm-RtKan--LRfeq9_cBY5PqrjiHTFJJ89Eg4iLbTagKeiDiZ9sozUNtn0u6hD2tMDynrU7pI9uyFIkPdU9ratku8tOgKWLFchRCQ1UD5Knda3F7fW0V4sCxfVqpuCZIROj7zAoB-RCMxkubiO6CyMZA19sunUQRwnp71DXcUqbZK-_jhef_hBQBUX1oaEojYtua4jx5p8xo9nP7jJeK-xqCj_CAoQF4LomezuGojgvxOoAQKBgQDSsCdKL_aFO7ONJSMgRGpNT9MGOMfKmAfPuELDOqRmJyTfbyR8TUiDYq74L3ffgjPIjZlJJM8m5gpCGalmsxRMUyvsDXE_bU2Zb1jlj_FFSdC2y5eTVfac-Ihp1qISm-t8EwvE0qzNK2xbG3G6ijy4WOtMoUh6FfZhzfmbxDgoiQKBgQChFtqYFu--khcyd26GVjxdDyPyiQfyAyqwdaWHqjyYad0sjEgZDaaAF_2qvrkSuD7kYULI9nVZv7kJmQu-T8owf38Hz931r9GaJdJJSTvexinJ54T0GpJdPsOUosHHgfPFvyetl1pxD05GMt88Z36KUUcZXVnoJxS9mo7HpoBQgQKBgAfJRspxF1U5LZuLwc6ReLQ-vPe_5XJRSAifMKhyZFz6GVzAiMKnQITKgtjdODrkXvGMehu_5n_zhHGI7T_EYn2nnTnuDT9g1LtU6B4jwbDj13jJ8WIajTCj5rayne6-IGfHdGnjt0slza1YSE2yiift8VQ1qa4JXb-jkxP0nnaxAoGAO4t4F9n6msXjnzr4dt2viHKNRhyS_ElhYULLgh9SMMCJCet8xw39qsGzeYbwYFQMo1y0VBaOADPXUQ3qgll6En0-VoPmtudbohAy7_YLFGjJj6FtytF7os4Ne4bB_F4z3revEgKtYrdWpqotTGWxJ62ti1mvXxn7F67m8jPAoIECgYEA0dGY2JnYVIku9hOYTTzjAJCRqA2Exl4lzxLYyD1SG_gTly9cee77m4wHOwYpLrRAu8zwLK5_4EkKDk_AKAVP9-lbqIWo7LG3KQ8OAbaJ3XnF4-ildPGQWXlpusDZQhYTFZIbbZ7zhi30A3XiZUqVMBkn8y1LdFmgj7Ogb4_87K4" ;

//        String BASE_URL = "http://10.151.30.80:18064/yop-center/";
//        String BASE_URL = "https://58.83.141.56/yop-center/";

//        YopConfig.setConnectTimeout(1);
//        YopConfig.setReadTimeout(1);

        String appKey = "OPR:10012481831";
        YopRequest request = new YopRequest(appKey, null);
        // YopRequest request = new YopRequest(appKey, "");

        request.addParam("customerNo", "10012481831");
        request.addParam("parentCustomerNo", "10012481831");
        request.addParam("requestId", "requestId1480392119078");
        request.addParam("uniqueOrderNo", "1001201611290000000000000808");
        YopResponse response = YopClient3.postRsa("/rest/v2.0/opr/queryorder", request);
        AssertUtils.assertYopResponse(response);
    }

}
