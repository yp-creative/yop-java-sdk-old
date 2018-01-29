package com.yeepay.g3.sdk.yop.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.Digest;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.DateUtils;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <pre>
 * 对称加密 Client，简化用户发起请求及解析结果的处理
 * </pre>
 *
 * @author baitao.ji
 * @version 2.0
 */
public class YopClient extends AbstractClient {

    private static final Logger LOGGER = Logger.getLogger(YopClient.class);

    private static final Joiner queryStringOldJoiner = Joiner.on("");

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String apiUri, YopRequest request) throws IOException {
        String content = postForString(apiUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起post请求，以字符串返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 字符串形式的响应
     */
    public static String postForString(String apiUri, YopRequest request) throws IOException {
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(request);

        RequestBuilder requestBuilder = RequestBuilder.post().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : request.getParams().entries()) {
            requestBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        HttpUriRequest httpPost = requestBuilder.build();
        return fetchContentByApacheHttpClient(httpPost);
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse get(String apiUri, YopRequest request) throws IOException {
        String content = getForString(apiUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起get请求，以字符串返回
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 字符串形式的响应
     */
    public static String getForString(String apiUri, YopRequest request) throws IOException {
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(request);

        RequestBuilder requestBuilder = RequestBuilder.get().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : request.getParams().entries()) {
            requestBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        HttpUriRequest httpGet = requestBuilder.build();
        return fetchContentByApacheHttpClient(httpGet);
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse upload(String apiUri, YopRequest request) throws IOException, URISyntaxException {
        String content = uploadForString(apiUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起文件上传请求，以字符串返回
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 字符串形式的响应
     */
    public static String uploadForString(String apiUri, YopRequest request) throws IOException, URISyntaxException {
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(request);

        RequestBuilder requestBuilder = RequestBuilder.post().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        if (!request.hasFiles()) {
            for (Map.Entry<String, String> entry : request.getParams().entries()) {
                requestBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        } else {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for (Object file : request.getMultiportFiles()) {
                if (file instanceof String) {
                    multipartEntityBuilder.addBinaryBody("_file", new File((String) file));
                } else if (file instanceof File) {
                    multipartEntityBuilder.addBinaryBody("_file", (File) file);
                } else {
                    multipartEntityBuilder.addBinaryBody("_file", (InputStream) file, ContentType.DEFAULT_BINARY, generateFileName());
                }
            }
            for (Map.Entry<String, String> entry : request.getParams().entries()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
            }
            requestBuilder.setEntity(multipartEntityBuilder.build());
        }

        HttpUriRequest httpPost = requestBuilder.build();
        return fetchContentByApacheHttpClient(httpPost);
    }

    /**
     * 适用于 AES 签名方式
     *
     * @param request
     * @param forSignature
     * @return
     */
    public static String getCanonicalQueryString(YopRequest request, boolean forSignature) {
        Multimap<String, String> parameters = request.getParams();
        if (parameters.isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, Collection<String>> entry : parameters.asMap().entrySet()) {
            if (forSignature &&
                    (Headers.AUTHORIZATION.equalsIgnoreCase(entry.getKey()) || request.getIgnoreSignParams().contains(entry.getKey()))) {
                continue;
            }
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");
            Collection<String> values = entry.getValue();
            for (String value : values) {
                if (value == null) {
                    parameterStrings.add(key);
                } else {
                    parameterStrings.add(key + value);
                }
            }
        }
        Collections.sort(parameterStrings);

        return queryStringOldJoiner.join(parameterStrings);
    }

    private static void normalize(YopRequest request) {
        // 归一化
        if (!request.getHeaders().containsKey(Headers.YOP_REQUEST_ID)) {
            request.addHeader(Headers.YOP_REQUEST_ID, UUID.randomUUID().toString());
        }

        String timestamp = DateUtils.formatCompressedIso8601Timestamp(System.currentTimeMillis());
        request.addHeader(Headers.YOP_DATE, timestamp);
    }

    private static void sign(YopRequest request) {
        String canonicalQueryString = getCanonicalQueryString(request, true);

        String secret = request.getSecretKey();
        String algName = request.getSignAlg();
        algName = StringUtils.isBlank(algName) ? YopConstants.ALG_SHA1 : algName;

        String sb = secret + canonicalQueryString + secret;
        String signature = Digest.digest(sb, algName);
        if (32 == Base64.decodeBase64(secret).length) {
            request.addHeader("Authorization", "YOP-HMAC-AES256 " + signature);
        } else {
            request.addHeader("Authorization", "YOP-HMAC-AES128 " + signature);
        }
//        request.getParams().put("sign", signature);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("canonicalQueryString:" + canonicalQueryString);
            LOGGER.debug("signature:" + signature);
        }
    }

    private static void handleResult(YopRequest request, YopResponse response) {
        String stringResult = response.getStringResult();
        if (StringUtils.isNotBlank(stringResult)) {
            response.setResult(JacksonJsonMarshaller.unmarshal(stringResult, Object.class));
        }

        String sign = response.getSign();
        if (StringUtils.isNotBlank(sign)) {
            response.setValidSign(verifySignature(request, response, sign));
        }
    }

    /**
     * 校验签名
     *
     * @param response
     * @param expectedSign
     * @return
     */
    private static boolean verifySignature(YopRequest request, YopResponse response, String expectedSign) {
        String trimmedBizResult = response.getStringResult().replaceAll("[ \t\n]", "");
        StringBuilder sb = new StringBuilder();
        sb.append(request.getSecretKey());
        sb.append(StringUtils.trimToEmpty(response.getState() + trimmedBizResult + response.getTs()));
        sb.append(request.getSecretKey());
        String calculatedSign = Digest.digest(sb.toString(), StringUtils.isBlank(request.getSignAlg()) ? YopConstants.ALG_SHA1 : request.getSignAlg());
        return StringUtils.equalsIgnoreCase(expectedSign, calculatedSign);
    }

    /**
     * //TODO 商户通知重新定义新二进制协议
     * -------------------------商户通知--------------------------------------------------
     */
    public static String acceptNotificationAsJson(String key, String response) {
        return validateAndDecryptNotification(key, response);
    }

    public static Map acceptNotificationAsMap(String key, String response) {
        String s = acceptNotificationAsJson(key, response);
        return s == null ? null : JsonUtils.fromJsonString(acceptNotificationAsJson(key, response), Map.class);
    }

    private static String validateAndDecryptNotification(String key, String response) {
        Map map = JsonUtils.fromJsonString(response, Map.class);
        boolean doEncryption = Boolean.valueOf(map.get("doEncryption").toString());
        String encryption = map.get("encryption").toString();
        String signature = map.get("signature").toString();
        String signatureAlg = map.get("signatureAlg").toString();

        //如果加密，解密
        if (doEncryption) {
            encryption = AESEncrypter.decrypt(encryption, key);
        }

        //签名是必须的...
        String localSignature = Digest.digest(key + encryption + key, signatureAlg);
        //验签失败...
        if (!localSignature.equals(signature)) {
            return null;
        }
        return encryption;
    }
}
