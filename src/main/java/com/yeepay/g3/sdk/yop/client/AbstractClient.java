package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class AbstractClient {

    private static final Logger LOGGER = Logger.getLogger(AbstractClient.class);

    private static final String REST_PREFIX = "/rest/v";

    private static CloseableHttpClient httpClient;

    static {
        initApacheHttpClient();
    }

    // 创建包含connection pool与超时设置的client
    public static void initApacheHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(InternalConfig.READ_TIMEOUT)
                .setConnectTimeout(InternalConfig.CONNECT_TIMEOUT)
                .build();

        httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(InternalConfig.MAX_CONN_TOTAL)
                .setMaxConnPerRoute(InternalConfig.MAX_CONN_PER_ROUTE)
                .setSSLSocketFactory(InternalConfig.TRUST_ALL_CERTS ? getTrustedAllSSLConnectionSocketFactory() : null)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static void destroyApacheHttpClient() {
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("httpclient close fail", e);
        }
    }

    private static SSLConnectionSocketFactory getTrustedAllSSLConnectionSocketFactory() {
        LOGGER.warn("[yop-sdk]已设置信任所有证书。仅供内测使用，请勿在生产环境配置。");
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception e) {
            LOGGER.error("error when get trust-all-certs request factory,will return normal request factory instead", e);
        }
        return sslConnectionSocketFactory;
    }

    protected static YopResponse fetchContentByApacheHttpClient(HttpUriRequest request) throws IOException {
        CloseableHttpResponse remoteResponse = getHttpClient().execute(request);
        HttpEntity resEntity = null;
        try {
            // 判断返回值
            int statusCode = remoteResponse.getStatusLine().getStatusCode();
            if (statusCode >= 400) {
                throw new YopClientException(Integer.toString(statusCode));
            }

            String content = EntityUtils.toString(remoteResponse.getEntity());
            YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);

            Header requestIdHeader = remoteResponse.getFirstHeader("x-yop-request-id");
            if (null != requestIdHeader) {
                response.setRequestId(requestIdHeader.getValue());
            }
            return response;
        } finally {
            HttpClientUtils.closeQuietly(remoteResponse);
        }
    }

    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    protected static String generateFileName() {
        return System.currentTimeMillis() + "-yop-" + RandomStringUtils.randomNumeric(8) + ".bin";
    }

    protected static String richRequest(String methodOrUri, YopRequest request) {
        Assert.hasText(methodOrUri, "method name or rest uri");
        String serverRoot = request.getAppSdkConfig().getServerRoot();

        String path = methodOrUri;
        if (StringUtils.startsWith(methodOrUri, serverRoot)) {
            path = StringUtils.substringAfter(methodOrUri, serverRoot);
        }

        if (!StringUtils.startsWith(path, REST_PREFIX)) {
            throw new YopClientException("Unsupported request method.");
        }

        /*v and method are always needed because of old signature implementation...*/
        request.setParam(YopConstants.VERSION, StringUtils.substringBetween(methodOrUri, REST_PREFIX, "/"));
        request.setParam(YopConstants.METHOD, methodOrUri);
        return serverRoot + path;
    }

}
