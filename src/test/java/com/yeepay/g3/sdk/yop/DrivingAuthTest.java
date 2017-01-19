package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopConfig;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.junit.Test;

/**
 * 类名称: DrivingAuthTest <br>
 * 类描述: <br>
 *
 * @author guoliang.li
 * @version 1.0.0
 * @since 16/8/22 下午2:49
 */
public class DrivingAuthTest {

    @Test
    public void drivingAuth() throws Exception {
        YopConfig.setAppKey("test");//yop应用
        YopConfig.setAesSecretKey("LVLDflZNINrrCFPIis9gCA==");//yop应用密钥，需要和短信通知应用的密钥保持一致才行，否则验证签名不通过
        YopConfig.setServerRoot("http://172.17.102.173:8064/yop-center");
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA-256");//具体看api签名算法而定
        request.setEncrypt(false);
        request.addParam("licenseNumber", "360121198605075212");
        request.addParam("name", "万汉波");
        request.addParam("requestSystem", "auth2-boss");
        request.addParam("requestCustomerId", "guoliang.li");
        request.addParam("requestFlowId", System.currentTimeMillis() + "");
        request.addParam("requestIP", "172.0.0.1");
        request.addParam("requestIdentification", "guoliang.li");
        YopResponse response = YopClient.post("/rest/v1.0/auth/drivingLicense", request);
        System.out.println(response);
    }


}
