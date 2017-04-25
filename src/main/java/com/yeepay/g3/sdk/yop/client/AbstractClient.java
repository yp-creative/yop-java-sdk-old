package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyStore;
import java.security.KeyStoreException;

public class AbstractClient {

    private static final Logger LOGGER = Logger.getLogger(AbstractClient.class);

    protected static RestTemplate restTemplate;

    protected static RestTemplate cfcaRestTemplate;

    static {
        init();
    }

    private static void init() {
        int connectTimeout = 30000;
        int readTimeout = 60000;

        /*initialize connect timeout: YopConfig >> yop_sdk_config.json >> 10000*/
        if (InternalConfig.CONNECT_TIMEOUT >= 0) {
            connectTimeout = InternalConfig.CONNECT_TIMEOUT;
        }

        /*initialize read timeout: YopConfig >> yop_sdk_config.json >> 30000*/
        if (InternalConfig.READ_TIMEOUT >= 0) {
            readTimeout = InternalConfig.READ_TIMEOUT;
        }

        /*--------------------initialize the normal restTemplate start------------------------------*/
        HttpComponentsClientHttpRequestFactory normalRequestFactory = new HttpComponentsClientHttpRequestFactory();
        normalRequestFactory.setConnectTimeout(connectTimeout);
        normalRequestFactory.setReadTimeout(readTimeout);
        restTemplate = new RestTemplate(normalRequestFactory);
        /*--------------------initialize the normal restTemplate end--------------------------------*/


        /*--------------------initialize the cfca restTemplate start--------------------------------*/
        KeyStore clientKeyStore = getKeyStore(true);
        KeyStore trustStore = getKeyStore(false);

        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        if (InternalConfig.CLIENT_CERTIFICATE != null) {
            try {
                clientKeyStore.load(InternalConfig.getInputStream(InternalConfig.CLIENT_CERTIFICATE.getPath()), InternalConfig.CLIENT_CERTIFICATE.getPassword().toCharArray());
                sslContextBuilder.loadKeyMaterial(clientKeyStore, InternalConfig.CLIENT_CERTIFICATE.getPassword().toCharArray());
            } catch (Exception e) {
                LOGGER.error("error happen when loading client certificate", e);
                return;
            }
        }

        if (InternalConfig.TRUST_CERTIFICATE != null) {
            try {
                trustStore.load(InternalConfig.getInputStream(InternalConfig.TRUST_CERTIFICATE.getPath()), InternalConfig.TRUST_CERTIFICATE.getPassword().toCharArray());
                sslContextBuilder.loadTrustMaterial(trustStore);
            } catch (Exception e) {
                LOGGER.error("error happen when loading trust certificate", e);
                return;
            }
        }

        SSLConnectionSocketFactory sslsf;
        try {
            sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            LOGGER.error("error create ssl connection socket factory", e);
            return;
        }
        HttpClient cfcaHttpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        HttpComponentsClientHttpRequestFactory cfcaRequestFactory = new HttpComponentsClientHttpRequestFactory(cfcaHttpClient);
        //TODO fix this
//        cfcaRequestFactory.setConnectTimeout(connectTimeout);
//        cfcaRequestFactory.setReadTimeout(readTimeout);
        cfcaRestTemplate = new RestTemplate(cfcaRequestFactory);


       /*--------------------initialize the cfca restTemplate end-----------------------------------*/
    }

    private static KeyStore getKeyStore(boolean pkcs12) {
        final KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(pkcs12 ? "pkcs12" : KeyStore.getDefaultType());
            return keystore;
        } catch (KeyStoreException e) {
            /*should not be here*/
            e.printStackTrace();
        }
        return null;
    }

    /**
     * return the normal restTemplate or cfca restTemplate according to request.getUseCFCA()
     *
     * @param request
     * @return restTemplate
     */
    protected static RestTemplate getRestTemplate(YopRequest request) {
        return request.getUseCFCA() ? cfcaRestTemplate : restTemplate;
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
