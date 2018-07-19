package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class AbstractClient {

    private static final Logger LOGGER = Logger.getLogger(AbstractClient.class);

    private static final String REST_PREFIX = "/rest/v";

    private static CloseableHttpClient httpClient;

    private static org.apache.http.client.config.RequestConfig.Builder requestConfigBuilder;
    private static CredentialsProvider credentialsProvider;
    private static HttpHost proxyHttpHost;

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

        requestConfigBuilder = org.apache.http.client.config.RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(InternalConfig.CONNECT_TIMEOUT);
        requestConfigBuilder.setStaleConnectionCheckEnabled(true);
        /*if (InternalConfig.getLocalAddress() != null) {
            requestConfigBuilder.setLocalAddress(config.getLocalAddress());
        }*/

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
        CloseableHttpResponse remoteResponse = getHttpClient().execute(request, httpContext);
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
