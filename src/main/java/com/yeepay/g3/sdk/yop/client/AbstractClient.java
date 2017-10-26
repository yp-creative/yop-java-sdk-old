package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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

        boolean trustAllCerts = Boolean.valueOf(System.getProperty("yop.trust.all.certs", "false"));

        HttpComponentsClientHttpRequestFactory requestFactory = null;
        if (trustAllCerts) {
            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                });
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(builder.build())).build();
                requestFactory = new HttpComponentsClientHttpRequestFactory(httpclient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(connectTimeout);
            requestFactory.setReadTimeout(readTimeout);
        }

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
}
