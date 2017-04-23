package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.utils.Assert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/1/24 上午11:18
 */
public class YopBaseClient {

    protected static RestTemplate restTemplate = new YopRestTemplate();

    protected static RestTemplate getRestTemplate(YopRequest request) {
        if (null != request.getConnectTimeout() || null != request.getReadTimeout()) {
            int connectTimeout = getValueIfNotNull(request.getConnectTimeout(), YopConfig.getConnectTimeout());
            int readTimeout = getValueIfNotNull(request.getReadTimeout(), YopConfig.getReadTimeout());
            return new YopRestTemplate(connectTimeout, readTimeout);
        } else {
            return restTemplate;
        }
    }

    private static int getValueIfNotNull(Integer value, int defaultValue) {
        return null != value ? value : defaultValue;
    }

    /**
     * @param methodOrUri apiUri
     * @param request 请求对象
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

        if (!StringUtils.startsWith(path, "/rest/")) {
            throw new YopClientException("Unsupported request method.");
        }

        serverUrl += path;
        String version = StringUtils.substringBetween(methodOrUri, "/rest/v", "/");
        if (StringUtils.isNotBlank(version)) {
            request.setVersion(version);
        }

        return serverUrl;
    }

    /**
     * 从完整返回结果中获取业务结果，主要用于验证返回结果签名
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
