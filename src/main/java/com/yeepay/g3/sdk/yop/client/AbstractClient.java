package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.error.YopError;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpMethodName;
import com.yeepay.g3.sdk.yop.http.YopHttpResponse;
import com.yeepay.g3.sdk.yop.model.YopErrorResponse;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.FileUtils;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import com.yeepay.g3.sdk.yop.utils.checksum.CRC64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.CheckedInputStream;

public class AbstractClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

    private static final String[] API_URI_PREFIX = {"/rest/v", "/yos/v"};

    private static final String CONTENT_TYPE_JSON = "application/json";

    private static CloseableHttpClient httpClient;

    private static org.apache.http.client.config.RequestConfig.Builder requestConfigBuilder;
    private static CredentialsProvider credentialsProvider;
    private static HttpHost proxyHttpHost;

    protected static final String SESSION_ID = getUUID();

    private static final YopError FILE_CHECK_ERROR;

    static {
        initApacheHttpClient();
        FILE_CHECK_ERROR = new YopError();
        FILE_CHECK_ERROR.setCode("40044");
        FILE_CHECK_ERROR.setMessage("业务处理失败");
        FILE_CHECK_ERROR.setSubCode("isv.scene.filestore.put.crc-failed");
        FILE_CHECK_ERROR.setSubMessage("文件上传crc校验失败");
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

    /**
     * 构建普通formHttp请求
     *
     * @param request    yop请求
     * @param contentUrl 请求地址
     * @param httpMethod http方法
     * @return http请求
     * @throws IOException io异常
     */
    protected static HttpUriRequest buildFormHttpRequest(YopRequest request, String contentUrl, HttpMethodName httpMethod) throws IOException {
        RequestBuilder requestBuilder;
        if (HttpMethodName.POST == httpMethod) {
            requestBuilder = RequestBuilder.post();
        } else if (HttpMethodName.GET == httpMethod) {
            requestBuilder = RequestBuilder.get();
        } else {
            throw new YopClientException("unsupported http method");
        }
        requestBuilder.setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : request.getParams().entries()) {
            requestBuilder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
        }
        return requestBuilder.build();
    }

    /**
     * 构建multiFormRequest
     *
     * @param request    yop请求
     * @param contentUrl 请求地址
     * @return key为http请求，value为checkInputStream列表
     * @throws IOException io异常
     */
    protected static Pair<HttpUriRequest, List<CheckedInputStream>> buildMultiFormRequest(YopRequest request, String contentUrl) throws IOException {
        RequestBuilder requestBuilder = RequestBuilder.post().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        TreeMap<String, CheckedInputStream> checkedInputStreams = null;
        if (!request.hasFiles()) {
            for (Map.Entry<String, String> entry : request.getParams().entries()) {
                requestBuilder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
            }
        } else {
            checkedInputStreams = Maps.newTreeMap();
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setCharset(Charset.forName(YopConstants.ENCODING));
            for (Map.Entry<String, Object> entry : request.getMultipartFiles().entrySet()) {
                String paramName = entry.getKey();
                Pair<String, CheckedInputStream> checkedInputStreamPair = wrapToCheckInputStream(entry.getValue());
                multipartEntityBuilder.addBinaryBody(paramName, checkedInputStreamPair.getRight(), ContentType.DEFAULT_BINARY, checkedInputStreamPair.getLeft());
                checkedInputStreams.put(paramName, checkedInputStreamPair.getRight());
            }
            for (Map.Entry<String, String> entry : request.getParams().entries()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
            }
            requestBuilder.setEntity(multipartEntityBuilder.build());
        }

        HttpUriRequest httpPost = requestBuilder.build();
        List<CheckedInputStream> inputStreamList = checkedInputStreams == null ? null : new ArrayList<CheckedInputStream>(checkedInputStreams.values());
        return new ImmutablePair<HttpUriRequest, List<CheckedInputStream>>(httpPost, inputStreamList);

    }

    /**
     * 包装为checkInputStream
     *
     * @param file 文件或者流
     * @return key为文件名称，value为流
     * @throws IOException io异常
     */
    private static Pair<String, CheckedInputStream> wrapToCheckInputStream(Object file) throws IOException {
        if (file instanceof String) {
            File paramFile = new File((String) file);
            CheckedInputStream inputStream = new CheckedInputStream(new FileInputStream(paramFile), new CRC64());
            return new ImmutablePair<String, CheckedInputStream>(paramFile.getName(), inputStream);
        }
        if (file instanceof File) {
            CheckedInputStream inputStream = new CheckedInputStream(new FileInputStream((File) file), new CRC64());
            return new ImmutablePair<String, CheckedInputStream>(((File) file).getName(), inputStream);
        }
        if (file instanceof InputStream) {
            String fileName = FileUtils.getFileName((InputStream) file);
            CheckedInputStream inputStream = new CheckedInputStream((InputStream) file, new CRC64());
            return new ImmutablePair<String, CheckedInputStream>(fileName, inputStream);
        }
        throw new YopClientException("不支持的上传文件类型");
    }


    protected static YopResponse fetchContentByApacheHttpClient(HttpUriRequest request) throws IOException {
        HttpContext httpContext = createHttpContext();
        CloseableHttpResponse remoteResponse = null;
        try {
            remoteResponse = getHttpClient().execute(request, httpContext);
            return parseResponse(remoteResponse);
        } finally {
            if (null != remoteResponse && isJsonResponse(remoteResponse)) {
                HttpClientUtils.closeQuietly(remoteResponse);
            }
        }
    }

    protected static YopResponse parseResponse(CloseableHttpResponse response) throws IOException {
        YopHttpResponse httpResponse = new YopHttpResponse(response);
        if (httpResponse.getStatusCode() / 100 == HttpStatus.SC_OK / 100) {
            //not a error
            YopResponse yopResponse = new YopResponse();
            handleHeaders(yopResponse, response);
            yopResponse.setState("SUCCESS");
            yopResponse.setRequestId(httpResponse.getHeader(Headers.YOP_REQUEST_ID));
            if (httpResponse.getContent() != null) {
                if (isJsonResponse(response)) {
                    String result = IOUtils.toString(httpResponse.getContent(), YopConstants.ENCODING);
                    JacksonJsonMarshaller.load(result, yopResponse);
                    if (yopResponse.getStringResult() != null) {
                        yopResponse.setResult(JacksonJsonMarshaller.unmarshal(yopResponse.getStringResult(), Object.class));
                    }
                } else {
                    yopResponse.setResult(response.getEntity().getContent());
                }
            }
            yopResponse.setValidSign(true);
            return yopResponse;
        } else if (httpResponse.getStatusCode() >= 500) {
            if (httpResponse.getContent() != null) {
                YopResponse yopResponse = new YopResponse();
                handleHeaders(yopResponse, response);
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

    private static boolean isJsonResponse(CloseableHttpResponse response) {
        return StringUtils.startsWith(response.getEntity().getContentType().getValue(), CONTENT_TYPE_JSON);
    }

    /**
     * 填充header
     *
     * @param yopResponse 业务response
     * @param response    httpResponse
     */
    private static void handleHeaders(YopResponse yopResponse, CloseableHttpResponse response) {
        HeaderIterator headerIterator = response.headerIterator();
        while (headerIterator.hasNext()) {
            Header header = headerIterator.nextHeader();
            if (StringUtils.startsWith(header.getName(), Headers.YOP_PREFIX)) {
                yopResponse.addHeader(header.getName(), header.getValue());
            }
        }
    }

    protected static void checkFileIntegrity(YopResponse response, String crc64) {
        if (response.isSuccess()) {
            String responseCrc64 = response.getHeaders().get(Headers.YOP_HASH_CRC64ECMA);
            if (null == responseCrc64 || StringUtils.equals(responseCrc64, crc64)) {
                return;
            }
            response.setState("FAILURE");
            response.setError(getFileCheckError());
        }
    }

    public static YopError getFileCheckError() {
        return FILE_CHECK_ERROR;
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
        Assert.hasText(methodOrUri, "apiUri");

        String requestRoot = MapUtils.isNotEmpty(request.getMultipartFiles()) ? request.getAppSdkConfig().getYosServerRoot() :
                request.getAppSdkConfig().getServerRoot();
        if (StringUtils.endsWith(requestRoot, "/")) {
            requestRoot = StringUtils.substring(requestRoot, 0, requestRoot.length() - 1);
        }

        String path = methodOrUri;
        if (StringUtils.startsWith(methodOrUri, requestRoot)) {
            path = StringUtils.substringAfter(methodOrUri, requestRoot);
        }

        if (!StringUtils.startsWithAny(path, API_URI_PREFIX)) {
            throw new YopClientException("Unsupported apiUri.");
        }

        /*v and method are always needed because of old signature implementation...*/
        request.setParam(YopConstants.VERSION, StringUtils.substringBefore(StringUtils.substringAfter(methodOrUri, "/v"), "/"));
        request.setParam(YopConstants.METHOD, methodOrUri);
        return requestRoot + path;
    }

    protected static String getUUID() {
        return UUID.randomUUID().toString();
    }

}
