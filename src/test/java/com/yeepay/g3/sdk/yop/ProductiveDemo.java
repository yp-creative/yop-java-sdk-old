package com.yeepay.g3.sdk.yop;

import com.TrustAllHttpsCertificates;
import com.yeepay.g3.facade.yop.ca.dto.DigitalEnvelopeDTO;
import com.yeepay.g3.facade.yop.ca.enums.CertTypeEnum;
import com.yeepay.g3.frame.yop.ca.DigitalEnvelopeUtils;
import com.yeepay.g3.frame.yop.ca.rsa.RSAKeyUtils;
import com.yeepay.g3.sdk.yop.client.*;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 15/7/8 10:23
 */
public class ProductiveDemo {

    private static final String[] APP_KEYS = {"yop-boss", "jinkela", "k1242692364"};
    private static final String[] APP_SECRETS = {"NWNZQslh36xJP2a5rodX1Q==", "T7aoGzBJrPDCL3qoo1ij/g==", "/7tTqP3OJr6A6j3mAUEFoQ=="};

    final String BASE_URL = "https://open.yeepay.com/yop-center/";

    @BeforeClass
    public static void setUp() throws Exception {
        TrustAllHttpsCertificates.setTrue();
    }

    @Test
    public void testIdCard() throws Exception {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i], "https://remit.yeepay.com/yop-center");
        request.setEncrypt(true);
        request.setSignRet(true);
//        request.setSignAlg("sha-256");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186691111");

        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
//        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testQueryMemberAccount() throws Exception {
        YopRequest request = new YopRequest(null,
                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a", BASE_URL);
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
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a", BASE_URL);
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
        YopRequest request = new YopRequest("yop-boss", "QFdODaBYBiVuLpP+sbyH+g==", BASE_URL);
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
                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a", BASE_URL);
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
                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a", BASE_URL);
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

        YopResponse response = YopClient.get("/rest/v1.0/member/gatewayDeposit", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void test1() {
        YopRequest request = new YopRequest(null,
                "s5KI8r0920SQ339oVlFE6eWJ0yk019SD7015nw39iaXJp10856z0C1d7JV5l", BASE_URL);
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
        YopRequest request = new YopRequest("TestAppKey002", "TestAppSecret002", BASE_URL);
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
        YopRequest request = new YopRequest("openSmsApi", "1234554321", BASE_URL);
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
        YopRequest request = new YopRequest("ypo2o", "tpcY6k2RSpEod7hsJIp33Q==", BASE_URL);
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
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==", BASE_URL);
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
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==", BASE_URL);
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
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==", BASE_URL);
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
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==", BASE_URL);
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
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i], BASE_URL);
        request.setEncrypt(false);
        request.setSignRet(true);
        request.setSignAlg("sha-256");

        YopResponse response = YopClient.post("/rest/v1.0/system/jvm", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLaike() {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0], BASE_URL);
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
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i], BASE_URL);
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
        YopRequest request = new YopRequest(appKey, null, BASE_URL);
        // YopRequest request = new YopRequest(appKey, "");

        request.addParam("customerNo", "10012481831");
        request.addParam("parentCustomerNo", "10012481831");
        request.addParam("requestId", "requestId1480392119078");
        request.addParam("uniqueOrderNo", "1001201611290000000000000808");
        YopResponse response = YopClient3.postRsa("/rest/v2.0/opr/queryorder", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testSongJinFeng() {
        String first = "mWNFrb3YuBTGpp_sIn9bRr_ZJSpvX-XbM_AaUiARsj28wgBGkir-CcnDDR1EtGTH15f4jgmmQDyhDbh3vgyF8X0BK-qzIVCyqLkolb9ba-LAJVO5NZ51kAXgAYtyi7kfM_Tpq1N3zHyJoWJ8lRFQboEBHX2w6IcLSOjm6DDoxkXtDTKw4VuzxUXRogXtnRRg2rfUDDWleVBhBxwZr8zNJhlEJiBmzdBZeSdvLoY1-ZH-9fW0YOQAgujUji_Va1F2kSeM3hO2T2NQyB0kZHfy92CNKWGqq343m0-vjcDHGTV-cPOLxi-v6jptHMtL22MULkWTMNls4-HiBKURqnGEHA$6denU1K5dafYu9VcCyVqr0BTIb3pViukGstIHoxuB2o32z9ULe5ZdY6NP2XCQhGNVOmelgey2A6-VdgvSNqhehQ4j3ZvuhB4ijDaoHeQOKnbJl6ltjxp1cxL-A3myVcD__tFy2uXgb5CABo5YeXsWf8kivmIcI5tHCvAJm6HqXq-Y1glAcQtRPA1BAziPwVFpDvquJhTNDgHSRtHhaKNy-fObsMK4L7jgj2xN97nrLPZW5kvAKHYd3RxeJB8ic4qjxDRVNXb4ht0i9Y1PkDPYr_rD3r_IuWRfAbX-PNh2aKQx2L1sHtASWBL8V5Ya0XgUl06HhG58nWkMVOZvjRhxCf5lQjJD9f7QHL3J-pK7n9MBEtfU0iui3mJqUGFBrIP2omXTxWN2e91v6LaK5g2QXv-RD53K-3yw2MzNSVjjkTfQC-irR8Z5EJvRCq1nYUweLez9mULoaFLULip1wvHeLG33pNeHaJcwJRdnmDDM1g-psdxz-8RfFeMGMGCj-nRLNUGzseBCtWeIjYYNgzZajGMQ136lZ2qSrTZeI-ERHd6LachIwmLdOcA05-h6MI8egSBb9JGTWFSs8vwgLHdtiUtMVcBXA58qmTArkXua4SkbJ1oEMY4q9u7tIyELsEdUo5eChm3dtxxzIQxvnNR-CvaDJdPz3UJl3S42iAn68XblMh8iUZF-rA6LM1D376dKWQaVmv0fS6COT8qbfmQSVz2PeJb9fRCIGu-1HxKZiabhn_W5Bnok36hdyy_syVhtkLM3JVM2l5glAkpbDvrwBYmgJ1d6jbNwaMhWDuzVTnE8QJwWOdNzeTjAdjKw8aE$AES$SHA256";
        String second = "Y9NUh84mMMr0hZxyrpRX1_ruASlWhNI9F0to—VtsqWnDTImu-Hs6uyGY3CORmQdFm2aH8CqdfyebImrdFWHivsfwxoHY1RNTV84KU-jyyq7Ip12CUhriru09wXxfqG373yKI1zjDEYHTSkiNlagoUNZd2M26YqjGaaJiLe2a12nw7dYJn_JGh3sScCzR8W7MJbOoCGh-9hmLnx-v0QaJuJvG0RkgkxHUk6EOZRNjuIUWZ404FHJWispE5_j-cCZAeoi2mf6JvkTAKb756mujB3vTqJZwsAfh3qsQlliA9VERkVnmSMv7pQjDjbcU3-5MKZxOKEGN7pwZvZw_GUCAQ$p-7sWz1XkrNuRJnFGSSxdAM2iNIiPWZagVwuYkaDr64LCmiIBCY24_p8onO3Jh5pWZLZDgYfdCBik1kAP_RhX-suf6hFKl3WLoV9Lyu6FQcNcvHpW6Yz5l5vwcjqY5rknUdQ_HNQa8oZgA_zgw-6FxoXPw7UT33hbA9LBfLEYobjjutm5ep95NcnBWlqHLULS64IuqN3Fm4-dMxCeLp-Mfgd1bqwxw9RNgXXoA3xq_DaibxuyWAeWts5iP5Ad9GD01sJmmdc9e8q8U60KINPUWgUXVt4Uo_6KLHuK3mRGJeQCXEEh2-2K1iGpiqqoOiD8QwOR8B7V1cpaOORbF-LFXzs0SzQsAObq3QNk-CT3eCmsfpiJlzPMvjpdBOJP1Mlj9fZd5P1bi5I83Nnqm85IBB-NayNU5jg_tquvqgXqCsZRw1za2ifEh_dxNu0Akry4oyRbj4SF2AYqNW70FRjpj5zjiiHGPdVAW3YAjd9YAt_qPkL4kj9CsTy7w-nJoZqa9KqJTz6H5eeXyxwQA6nA3v_SeH28dLUen6LJawrlVnXl0ivZusze8ECOK0eAzKNKas9EF_tsCtBzorw5iDESwcirhHRfyQy-ibqwzdQG7vE66mp_F3WwZmXyc3TUjgCgS6P2uStoV4F3CubtfoaCVtXgBwicmQHxkkEva-SFA-Pz3A92GAFE6e9EIUHIcHNSOSJ8NSqFX2eusQ1ZVj_TNj7X_KSUMp207GikGE8U4pzBo1Stlmso4tXdu4SSA1kVv-FUkS3J230hNFM_QnPRhplugbt8r_sHBGW0jkzjYLJ1M2MNBL0jLVseQuwG3Oa$AES$SHA256";

        DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
        dto.setCipherText(second);
        DigitalEnvelopeUtils.decrypt(dto, InternalConfig.getISVPrivateKey(CertTypeEnum.RSA2048), InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048));

        System.out.println(RSAKeyUtils.key2String(InternalConfig.getISVPrivateKey(CertTypeEnum.RSA2048)));
    }

    @Test
    public void testCfca() {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i], "https://remit.yeepay.com/yop-center");
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
        request.addParam("request_flow_id", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("id_card_number", "370982199101186691");
        YopResponse response = YopClient.post("/rest/v1.0/test/cfca", request);
        System.out.println(response);
    }

}
