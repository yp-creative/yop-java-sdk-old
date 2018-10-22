package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.error.YopError;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.YopHttpResponse;
import com.yeepay.g3.sdk.yop.model.YopErrorResponse;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AbstractClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

    private static final String REST_PREFIX = "/rest/v";

    private static CloseableHttpClient httpClient;

    private static org.apache.http.client.config.RequestConfig.Builder requestConfigBuilder;
    private static CredentialsProvider credentialsProvider;
    private static HttpHost proxyHttpHost;

    protected static final String SESSION_ID = getUUID();

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
                .evictExpiredConnections()
                .evictIdleConnections(5000, TimeUnit.MILLISECONDS)
                .setRetryHandler(new YopHttpRequestRetryHandler())
                .build();

        requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(InternalConfig.CONNECT_TIMEOUT);
        requestConfigBuilder.setStaleConnectionCheckEnabled(true);
        /*if (InternalConfig.getLocalAddress() != null) {
            requestConfigBuilder.setLocalAddress(config.getLocalAddress());
        }*/

        if (InternalConfig.proxy != null) {
            String proxyHost = InternalConfig.proxy.getHost();
            int proxyPort = InternalConfig.proxy.getPort();
            if (proxyHost != null && proxyPort > 0) {
                proxyHttpHost = new HttpHost(proxyHost, proxyPort);
                requestConfigBuilder.setProxy(proxyHttpHost);
                credentialsProvider = new BasicCredentialsProvider();
                String proxyUsername = InternalConfig.proxy.getUsername();
                String proxyPassword = InternalConfig.proxy.getPassword();
                String proxyDomain = InternalConfig.proxy.getDomain();
                String proxyWorkstation = InternalConfig.proxy.getWorkstation();
                if (proxyUsername != null && proxyPassword != null) {
                    credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
                            new NTCredentials(proxyUsername, proxyPassword,
                                    proxyWorkstation, proxyDomain));
                }
            }
        }
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
        HttpContext httpContext = createHttpContext();
        CloseableHttpResponse remoteResponse = null;
        try {
            remoteResponse = getHttpClient().execute(request, httpContext);
            return parseResponse(remoteResponse);
        } finally {
            HttpClientUtils.closeQuietly(remoteResponse);
        }
    }

    protected static YopResponse parseResponse(CloseableHttpResponse response) throws IOException {
        YopHttpResponse httpResponse = new YopHttpResponse(response);
        if (httpResponse.getStatusCode() / 100 == HttpStatus.SC_OK / 100) {
            //not a error
            YopResponse yopResponse = new YopResponse();
            yopResponse.setState("SUCCESS");
            yopResponse.setRequestId(httpResponse.getHeader(Headers.YOP_REQUEST_ID));
            if (httpResponse.getContent() != null) {
                yopResponse.setStringResult(IOUtils.toString(httpResponse.getContent(), YopConstants.ENCODING));
            }
            if (StringUtils.isNotBlank(yopResponse.getStringResult())) {
                yopResponse.setResult(JacksonJsonMarshaller.unmarshal(yopResponse.getStringResult(), Object.class));
            }
            yopResponse.setValidSign(true);
            return yopResponse;
        } else if (httpResponse.getStatusCode() >= 500) {
            if (httpResponse.getContent() != null) {
                YopResponse yopResponse = new YopResponse();
                yopResponse.setState("FAILURE");
                YopErrorResponse errorResponse = JacksonJsonMarshaller.unmarshal(httpResponse.getContent(),
                        YopErrorResponse.class);
                yopResponse.setRequestId(errorResponse.getRequestId());
                yopResponse.setError(YopError.Builder.anYopError()
                        .withCode(errorResponse.getCode())
                        .withSubCode(errorResponse.getSubCode())
                        .withMessage(errorResponse.getMessage())
                        .withSubMessage(errorResponse.getSubMessage())
                        .build());
                yopResponse.setValidSign(true);
                return yopResponse;
            } else {
                throw new YopClientException("empty result with httpStatusCode:" + httpResponse.getStatusCode());
            }
        }
        throw new YopClientException("unexpected httpStatusCode:" + httpResponse.getStatusCode());
    }

    /**
     * Creates HttpClient Context object based on the internal request.
     *
     * @return HttpClient Context object.
     */
    private static HttpClientContext createHttpContext() {
        HttpClientContext context = HttpClientContext.create();
        context.setRequestConfig(requestConfigBuilder.build());
        if (credentialsProvider != null) {
            context.setCredentialsProvider(credentialsProvider);
        }
        /*if (config.isProxyPreemptiveAuthenticationEnabled()) {
            AuthCache authCache = new BasicAuthCache();
            authCache.put(proxyHttpHost, new BasicScheme());
            context.setAuthCache(authCache);
        }*/
        return context;
    }

    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    protected static String richRequest(String methodOrUri, YopRequest request) {
        Assert.hasText(methodOrUri, "method name or rest uri");

        String requestRoot = MapUtils.isNotEmpty(request.getMultipartFiles()) ? request.getAppSdkConfig().getYosServerRoot() :
                request.getAppSdkConfig().getServerRoot();

        String path = methodOrUri;
        if (StringUtils.startsWith(methodOrUri, requestRoot)) {
            path = StringUtils.substringAfter(methodOrUri, requestRoot);
        }

        if (!StringUtils.startsWith(path, REST_PREFIX)) {
            throw new YopClientException("Unsupported request method.");
        }

        /*v and method are always needed because of old signature implementation...*/
        request.setParam(YopConstants.VERSION, StringUtils.substringBetween(methodOrUri, REST_PREFIX, "/"));
        request.setParam(YopConstants.METHOD, methodOrUri);
        return requestRoot + path;
    }

    protected static String getUUID() {
        return UUID.randomUUID().toString();
    }

}
