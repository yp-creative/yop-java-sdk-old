package com.yeepay.g3.sdk.yop.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * title: <br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 15/10/20 10:04
 */
public class YopRestTemplate extends RestTemplate {

    public YopRestTemplate() {
        this(YopConfig.getConnectTimeout(), YopConfig.getReadTimeout());
    }

    public YopRestTemplate(int connectTimeout, int readTimeout) {
        ClientHttpRequestFactory requestFactory = getRequestFactory();
        if (requestFactory instanceof SimpleClientHttpRequestFactory) {
            SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = (SimpleClientHttpRequestFactory) requestFactory;
            simpleClientHttpRequestFactory.setConnectTimeout(connectTimeout);
            simpleClientHttpRequestFactory.setReadTimeout(readTimeout);
        } else if (requestFactory instanceof HttpComponentsClientHttpRequestFactory) {
            HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = (HttpComponentsClientHttpRequestFactory) requestFactory;
//            httpComponentsClientHttpRequestFactory.setConnectTimeout(connectTimeout);
//            httpComponentsClientHttpRequestFactory.setReadTimeout(readTimeout);
        }
    }

}
