package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class AbstractClient {

    private static final Logger LOGGER = Logger.getLogger(AbstractClient.class);

    protected static final RestTemplate restTemplate;

    static {
        int connectTimeout = 30000;
        int readTimeout = 60000;

        if (InternalConfig.CONNECT_TIMEOUT >= 0) {
            connectTimeout = InternalConfig.CONNECT_TIMEOUT;
        }

        if (InternalConfig.READ_TIMEOUT >= 0) {
            readTimeout = InternalConfig.READ_TIMEOUT;
        }

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        restTemplate = new RestTemplate(requestFactory);
    }

    protected static RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            throw new IllegalStateException("restTemplate is not initialized!");
        }
        return restTemplate;
    }


    private static final String REST_PREFIX = "/rest/v";

    /**
     * @param methodOrUri apiUri
     * @param request     请求对象
     * @return 请求地址
     */
    protected static String richRequest(String methodOrUri, YopRequest request) {
        Assert.hasText(methodOrUri, "method name or rest uri");
        String serverUrl = request.getServerRoot();
        if (StringUtils.endsWith(serverUrl, "/")) {
            serverUrl = StringUtils.substring(serverUrl, 0, -1);
        }

        String path = methodOrUri;
        if (StringUtils.startsWith(methodOrUri, serverUrl)) {
            path = StringUtils.substringAfter(methodOrUri, serverUrl);
        }

        if (!StringUtils.startsWith(path, REST_PREFIX)) {
            throw new YopClientException("Unsupported request method.");
        }

        serverUrl += path;

        /*v and method are always needed because of old signature implementation...*/
        request.addParam(YopConstants.VERSION, StringUtils.substringBetween(methodOrUri, REST_PREFIX, "/"));
        request.addParam(YopConstants.METHOD, methodOrUri);
        return serverUrl;
    }

    /**
     * 从完整返回结果中获取业务结果，主要用于验证返回结果签名
     *
     * @param content 响应报文
     * @return 业务返回结果
     */
    protected static String getBizResult(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        String jsonStr = StringUtils.substringAfter(content, "\"result\" : ");
        jsonStr = StringUtils.substringBeforeLast(jsonStr, "\"ts\"");
        // 去除逗号
        jsonStr = StringUtils.substringBeforeLast(jsonStr, ",");
        return jsonStr;
    }
}
