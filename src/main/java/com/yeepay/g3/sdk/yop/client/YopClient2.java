package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.frame.yop.ca.utils.Encodes;
import com.yeepay.g3.sdk.yop.annotations.Exposed;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;

/**
 * <pre>
 * 功能说明：易宝开放平台(YeepayOpenPlatform简称YOP)SDK客户端，简化用户发起请求及解析结果的处理，包括加解密
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
@Deprecated
public class YopClient2 extends YopBaseClient {

    protected static final Logger logger = Logger.getLogger(YopClient2.class);

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse postBasic(String methodOrUri, YopRequest request) {
        String content = postBasicForString(methodOrUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        return response;
    }

    /**
     * 发起post请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String postBasicForString(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(methodOrUri, request);
        logger.info("signature:" + request.getParamValue(YopConstants.SIGN));
        request.encoding();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

        String authorizationHeader = "";
        try {
            authorizationHeader = Encodes.encodeBase64(("yop-boss:" + request.getSecretKey()).getBytes("utf-8")).trim();
        } catch (UnsupportedEncodingException e) {
            logger.warn("", e);
        }

        headers.add("Authorization", "Basic " + authorizationHeader);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(request.getParams(), headers);

        String content = getRestTemplate(request).postForObject(serverUrl, httpEntity, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

}
