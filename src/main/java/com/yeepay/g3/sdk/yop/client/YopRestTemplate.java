package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.frame.yop.ca.utils.Exceptions;
import com.yeepay.g3.sdk.yop.http.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    private static YopRestTemplate INSTANCE = null;

    public YopRestTemplate() {
        this(YopConfig.getConnectTimeout(), YopConfig.getReadTimeout());
    }

    public YopRestTemplate(int connectTimeout, int readTimeout) {
        super(YopRestTemplate.getClientHttpRequestFactory(connectTimeout, readTimeout));
    }

    public static ClientHttpRequestFactory getClientHttpRequestFactory(int connectTimeout, int readTimeout) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = null;
        try {
            CloseableHttpClient httpClient = HttpClientUtils.acceptsUntrustedCertsHttpClient(connectTimeout, readTimeout);
            clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        } catch (Exception e) {
            throw Exceptions.unchecked(e);
        }
        return clientHttpRequestFactory;
    }

    public static YopRestTemplate getRestTemplate() {
        synchronized (YopRestTemplate.class) {
            if (null == INSTANCE) {
                synchronized (YopRestTemplate.class) {
                    INSTANCE = new YopRestTemplate();
                }
            }
        }
        return INSTANCE;
    }

}
