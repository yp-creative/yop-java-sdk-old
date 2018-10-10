package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.hbird.HbirdLoginToken;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.utils.mapper.JsonMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

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
public class LocalDemo {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonDefaultMapper();

    private static final String[] APP_KEYS = {"yop-boss", "app_smtpZIFHFtrvLd9BR", "yop-boss"};
    private static final String[] APP_SECRETS = {"PdZ74F6sxapgOWJ31QKmYw==", "Iw4WPHO6hhjmOt7AoBP7HWWmdg6RMRsT6xZElc4RIAA=", "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCXsKWWClznZbdTwp9183e4Ygu/twbQhuS6LPpu/TZ+OFwwauvIZnOyKu+rFh6apKyVxiLEkssnTsBjLjUIlypEGU2SdLGkswWAPvVdunLjjWEz37W2w4VNkGf8bGCQ9fIxMynoBCTeeWcQz896e1y2p5YZHygUhXGLM/9q5mr3iQQgrEPdFEAdlfLexkbVIF2bS02NsDFLNvqKNk7219cefxWPgJfN7RukUIZyy4nbeevbMAAFpNUFh1NlAh4qzwocOfbZ3NgtwJDf29jibpM3dacS7tqYGwpeGpKazS9tZgTAYcX2kLT7s+G6vVzVQR61pvvDs5ubyfsw/KFR8KDDAgMBAAECggEAShSE6Z+p+4AbZhaYVbxPbYbEgh5af6BBOAMbUvTqlf3kV+j/uWD/g7WgUod87r0ZZBPdiu69tDarkkRQth9NDvDkh2/iCbM8LoOQxPN3hFXZcMICNn2KLnUls4siJelXHFwGTT8o2lWj1fwHMaPphXKWxTIIGu2IpBkC1iwtdTF8mqe2HH+H2djBE96JXVZIf3/FgGu8ppmXa/xG4DfrTxFnGEJzgaadT3Z+ybXbqjYgFgmmBnZOaTx1XPQfLGQVYJz9BunDhwhrqBUM+QuLr1jUsMsj/Yud52cNXjwq9z8FfkKUdVVfE4VrzH8JpKKk7Vim7RWBQER29jlEnV+ysQKBgQDjMWxZz4AveXxWSx7MgXN9PEzxzmGWSApseDskSi5PAmXa4ut5XyNJUiGJ8Zf+cssPfWFNtB7suJBuoMTtrQSap2tgoo70y7QSO0ZlZ0v5Ny9LYh8oHvDgBJVNmS5HWv1U1/VHxNHczNmQ05smXNo1bzMYe5Xo10J2W47UUTgOHwKBgQCq7G6B5RfD+O1jdmYWlilh5oi1XGdYJGnzhs9DmAUN5plQ3VxpUFxxQCgOwXCskfT9QUVYhsIpQIs2iCylwuNDuxxiEQyRpeBirRaqmxvosv08Trwsr1Vs/Cuh17ZZOS+OUehN0fDZCiruK4e2btVfv8LlE1KMuoiUsn1X2gWQ3QKBgCyqBrcRSA4NQBhm5EMoH+A6/pV7EUxOFV6FtHrJ6pi1y/hgLBLMVU+Qye8og80OHEWLTJnOE1ZOYnadPJnNLd6Jk16IFrqhYWFELe65hAIWi0GypJVqn8gqnn+G4cY9aRhI7HuTgf56dzs1nobIMk3W8qCZizsfNn22OjobTX3ZAoGBAJsTusvF1IMs5g05DjTt9wvpQx3xgZ46I5sdNA3q7qMHFxGEVeUDUWw7Plzs61LXdoUU5FsGoUEWW3iVopSett3r9TuQpmu7KVO+IXOXGYJOa259LUQJrKMeRGQpuDtJpDknXXLFyRTSodLH0fEWrCecb7KxjlM6ptLrAshjemtNAoGBAMzGo6aNER8VZfET8Oy0i5G8aVBp6yrMiQsNOj4S1VPoHI+Pc6ot5rDQdjek9PRzF9xeCU4K7+KLaOs6fVmTfsFpPbDafCTTmos9LGr5FIyXpU7LQCl3QPHWPDd5ezsu9SPVjzsEPX3WTSOJuUA8hE7pJnAzMHLGAFpIXJRu3Z/y"};

    @BeforeClass
    public static void setUp() throws Exception {
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_local.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_dev.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_qa_docker.json");
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_pro.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_pro_docker.json");
//        System.setProperty("yop.sdk.trust.all.certs", "true");
    }

    @Test
    public void test112() throws IOException {
        String merchant = "yop";
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
        request.addParam("requestNo", "trx" + RandomStringUtils.randomNumeric(10));
        request.addParam("merchantUserId", merchant);
        YopResponse response = YopClient.post("/rest/v1.0/payplus/user/register", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAES_BASE64_URLEncode() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "czr+7xY");

        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAES_SHA1() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha1");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186692");

        request.addHeader("Accept-Encoding", "gzip");

        YopResponse response = YopClient.get("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);

        response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAES_SHA256() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient.get("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);

        response = YopClient.post("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAES256_SHA256() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.setSignAlg("sha-256");
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient.get("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);

        response = YopClient.post("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testCreateToken() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA-256");

        request.addParam("grant_type", "password");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "unit test");
        request.addParam("scope", "test");

        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test(timeout = 30000)
    public void testUpLoadFileOld() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");
//        request.addParam("_file", "file:/Users/xxx/1.png");
        request.addParam("_file", "src/test/resources/log4j.xml");

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testUpLoadFileNew1() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");

        request.addFile("src/test/resources/log4j.xml");

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testUpLoadFileNew2() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");

        request.addFile(new File("src/test/resources/log4j.xml"));

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testUpLoadFileNew3() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");

        FileInputStream stream = new FileInputStream(new File("/Users/dreambt/SiteMesh Flow Diagram.png"));
        request.addFile(stream);

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);

        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".png"));
    }

    @Test(timeout = 50000)
    public void testUpLoadFileNew4() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.setSignAlg("SHA-256");
        request.addParam("corp_id", "356e1dc1-4c11-419b-a043-cccb537dfb9b");
        request.addParam("user_name", "baitao.ji");
        request.addParam("password", "yeepay.com");
        request.addParam("need_corp_info", "true");
        request.addParam("need_token", "true");
        request.addParam("verified", "true");

        YopResponse response = YopClient.post("/rest/v2.0/hbird/oauth2/token", request);
        AssertUtils.assertYopResponse(response);

        YopRequest request2 = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request2.addParam("target_id", "010b3c35-f384-493f-919d-67177f70dda7");
        request2.addParam("target_type", "USER");
//        request2.addParam("fileBase64", Base64.encodeBase64String(IOUtils.toByteArray(new FileInputStream(new File("/Users/dreambt/test2.txt")))));

        HbirdLoginToken hbirdLoginToken = JSON_MAPPER.fromJson(response.getStringResult(), HbirdLoginToken.class);
        assert null != hbirdLoginToken.getoAuth2AccessToken();
        request2.addHeader("Authorization", "Bearer " + hbirdLoginToken.getoAuth2AccessToken().getValue());

        request2.addFile("fileBase64", new FileInputStream(new File("/Users/dreambt/test2.txt")));

        response = YopClient.upload("/rest/v2.0/hbird/sharefile/upload", request2);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testRsa() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient3.postRsa("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testRsa2() throws Exception {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient3.postRsa("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testRsa3() throws Exception {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient3.postRsa("/rest/v1.0/sys/trade/order", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test(timeout = 30000)
    public void testRsaUploadFile() throws IOException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");
//        request.addParam("_file", "file:/Users/dreambt/xuekun-3.pfx");
        request.addParam("_file", "src/test/resources/log4j.xml");

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNew1() throws IOException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        request.addFile("src/test/resources/log4j.xml");

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNew2() throws IOException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        request.addFile(new File("src/test/resources/log4j.xml"));

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNew3() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        FileInputStream stream = new FileInputStream(new File("/Users/dreambt/SiteMesh Flow Diagram.png"));
        request.addFile(stream);

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);

        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".png"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNewUseURL() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        request.addFile(new URL("https://www.baidu.com/img/bd_logo1.png").openStream());

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".png"));
    }

    @Test
    public void testOAuth2() throws Exception {
        YopRequest request = new YopRequest();
        request.addHeader(Headers.AUTHORIZATION, "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ5b3AiLCJqdGkiOiJhcHBfeE5UVTRORE13TURRMTphMzM1ZTMwNS1jYjUzLTQ4N2ItYmNhOS1kZGIwODhkYzFjMmEiLCJzdWIiOiJkODVlNzM2Yy0wNDZmLTRmMzctYjQyMC1jZjlhMTI1NDc0MjciLCJleHAiOjE1MjA0NzY0MTQsImNpZCI6IjQzIiwic2NvcGUiOltdLCJha19leHAiOjQzMjAwLCJya19leHAiOjI1OTIwMDAsInR5cGUiOiJyayIsInZlciI6IjIuMCJ9.tq87qVP2eOmr3em1MdwVwH0vkuET8MmQxJlkI-BM7IdgfNjMjB-yfJmtkAbgI6D7lOdsXmZxW13ZqS1j2WpWbQ");

        YopResponse response = YopClient.post("/rest/v2.0/hbird/qr/create-qr", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadLocalClass() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("className", "com.yeepay.g3.facade.notifier.NotifyFacade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadRemoteClass() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("className", "com.yeepay.g3.facade.opr.facade.yop.YopOrderFacade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadClassTimeout() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("className", "com.yeepay.g3.facade.ymf.facade.laike.OrderFacade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.contains("调用超过最大处理时长", response.getError().getMessage()));
    }

    @Test
    public void testConstraintViolationException() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "czr+7xY");

        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
        assertEquals("个人身份证号长度必须为15位或18位", response.getError().getMessage());
    }

    @Test
    public void testYeepayBizException() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA-256");

        request.addParam("grant_type", "password0");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "unit test");
        request.addParam("scope", "test");

        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        assertEquals("isp.code.invalid_grant", response.getError().getCode());
    }

    @Test
    public void testPayPlusRemitQuery() throws IOException {
        YopRequest request = new YopRequest();
        request.setEncrypt(true);
        request.setSignRet(true);
        request.setSignAlg("sha-256");
//        request.addParam("trxRequestNo","111");
        request.addParam("remitRequestNo", "Remit1534859751218");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/payplus/remit/query", request);
        System.out.println(response.toString());
    }

    @Test
    public void testNoProvider() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
//        request.addParam("trxRequestNo","111");
        request.addParam("remitRequestNo", "Remit1534859751218");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/mt-wallet/order/queryCodeInfo", request);
        System.out.println(response.toString());
    }

    @Test
    public void testDisabledApi() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
//        request.addParam("trxRequestNo","111");
        request.addParam("remitRequestNo", "Remit1534859751218");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/insordering-interface/car-insordering-interface/create-policy", request);
        System.out.println(response.toString());
    }

    @Test
    public void name() {
        YopRequest request = new YopRequest("yop-boss", "PdZ74F6sxapgOWJ31QKmYw==");
        request.setSignAlg("SHA-256");
        request.addParam("backendLatency", "100");

        YopResponse response = null;
        try {
            StopWatch stopWatch = StopWatch.createStarted();
            response = YopClient.post("/rest/v1.0/yop/mock/backend-latency", request);
            assertTrue(response.isSuccess());
//                assertTrue(response.isValidSign());
            stopWatch.stop();
            System.out.println("stopWatch:" + stopWatch.getTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
