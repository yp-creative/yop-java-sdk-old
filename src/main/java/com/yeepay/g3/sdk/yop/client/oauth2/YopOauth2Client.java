package com.yeepay.g3.sdk.yop.client.oauth2;

import com.yeepay.g3.sdk.yop.client.YopBaseClient;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.enums.HttpMethodType;
import com.yeepay.g3.sdk.yop.unmarshaller.YopMarshallerUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/2/20 下午1:57
 */
public class YopOauth2Client extends YopBaseClient {

    protected static final Logger logger = Logger.getLogger(YopOauth2Client.class);

    public static YopResponse postOauth2(String methodOrUri, YopOauth2Request request) {
        String content = postOauth2String(methodOrUri, request);
        YopResponse response = (YopResponse) YopMarshallerUtils.unmarshal(content, request.getFormat(), YopResponse.class);
        handleResult(request, response, content);
        return response;
    }

    public static String postOauth2String(String methodOrUri, YopOauth2Request request) {
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri, request);
        logger.info("signature:" + request.getParamValue("sign"));
        request.setAbsoluteURL(serverUrl);
        LinkedMultiValueMap headers = new LinkedMultiValueMap();
        headers.add("Authorization", "Bearer " + request.getSecretKey());
        headers.add("X-YOP-AppKey", "yop-boss");
        request.encoding();
        HttpEntity httpEntity = new HttpEntity(request.getParams(), headers);
        String content = (String) getRestTemplate(request).postForObject(serverUrl, httpEntity, String.class, new Object[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }

        return content;
    }

}
