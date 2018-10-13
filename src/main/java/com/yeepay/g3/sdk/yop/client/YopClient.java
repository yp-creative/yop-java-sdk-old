package com.yeepay.g3.sdk.yop.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.Digests;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.DateUtils;
import com.yeepay.g3.sdk.yop.utils.FileUtils;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import com.yeepay.g3.sdk.yop.utils.checksum.CRC64Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopClient.class);

    private static final Joiner queryStringOldJoiner = Joiner.on("");

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String apiUri, YopRequest request) throws IOException {
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(request);

        RequestBuilder requestBuilder = RequestBuilder.post().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : request.getParams().entries()) {
            requestBuilder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
        }

        HttpUriRequest httpPost = requestBuilder.build();
        YopResponse response = fetchContentByApacheHttpClient(httpPost);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse get(String apiUri, YopRequest request) throws IOException {
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(request);

        RequestBuilder requestBuilder = RequestBuilder.get().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : request.getParams().entries()) {
            requestBuilder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
        }

        HttpUriRequest httpGet = requestBuilder.build();
        YopResponse response = fetchContentByApacheHttpClient(httpGet);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse upload(String apiUri, YopRequest request) throws IOException, URISyntaxException {
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(request);

        RequestBuilder requestBuilder = RequestBuilder.post().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        if (!request.hasFiles()) {
            for (Map.Entry<String, String> entry : request.getParams().entries()) {
                requestBuilder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
            }
        } else {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for (Map.Entry<String, Object> entry : request.getMultipartFiles().entrySet()) {
                String paramName = entry.getKey();
                Object file = entry.getValue();
                if (file instanceof String) {
                    multipartEntityBuilder.addBinaryBody(paramName, new File((String) file));
                } else if (file instanceof File) {
                    multipartEntityBuilder.addBinaryBody(paramName, (File) file);
                } else if (file instanceof InputStream) {
                    String fileName = FileUtils.getFileName((InputStream) file);
                    multipartEntityBuilder.addBinaryBody(paramName, (InputStream) file, ContentType.DEFAULT_BINARY, fileName);
                } else {
                    throw new YopClientException("不支持的上传文件类型");
                }
            }
            for (Map.Entry<String, String> entry : request.getParams().entries()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
            }
            requestBuilder.setEntity(multipartEntityBuilder.build());
        }

        HttpUriRequest httpPost = requestBuilder.build();
        YopResponse response = fetchContentByApacheHttpClient(httpPost);
        handleResult(request, response);
        return response;
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
        Map<String, String> signParams = new TreeMap<String, String>();
        for (Map.Entry<String, Collection<String>> entry : parameters.asMap().entrySet()) {
            if (forSignature &&
                    (Headers.AUTHORIZATION.equalsIgnoreCase(entry.getKey()) || request.getIgnoreSignParams().contains(entry.getKey()))) {
                continue;
            }
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");
            List<String> list = new ArrayList<String>(entry.getValue());
            Collections.sort(list);
            signParams.put(key, StringUtils.join(list, ","));

        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            sb.append(entry.getKey()).append(entry.getValue());
        }
        return sb.toString();
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
        if (request.getHeaders().containsKey("Authorization")) {
            return;
        }
        if (request.hasFiles()) {
            try {
                request.addHeader(Headers.YOP_HASH_CRC64ECMA, CRC64Utils.calculateMultiPartFileCrc64ecma(request.getMultipartFiles()));
            } catch (IOException ex) {
                LOGGER.error("IOException occurred when generate crc64ecma.", ex);
                throw new YopClientException("IOException occurred when generate crc64ecma.", ex);
            }
        }

        String canonicalQueryString = getCanonicalQueryString(request, true);

        String secret = request.getAesSecretKey();
        String algName = request.getSignAlg();
        algName = StringUtils.isBlank(algName) ? YopConstants.ALG_SHA1 : algName;

        String sb = secret + canonicalQueryString + secret;
        String signature = Digests.digest2Hex(sb, algName);
        if (32 == Base64.decodeBase64(secret).length) {
            request.addHeader("Authorization", "YOP-HMAC-AES256 " + signature);
        } else {
            request.addHeader("Authorization", "YOP-HMAC-AES128 " + signature);
        }
//        request.getParams().put("sign", signature);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("========\ncanonicalQueryString:" + canonicalQueryString
                    + "\nmd5(secret):" + Digests.digest2Hex(secret, "md5")
                    + "\nsignature:" + signature);
        }
    }

    private static void handleResult(YopRequest request, YopResponse response) {
        String stringResult = response.getStringResult();
        if (StringUtils.isNotBlank(stringResult)) {
            response.setResult(JacksonJsonMarshaller.unmarshal(stringResult, Object.class));
        }
        verifySignature(request, response);
    }

    /**
     * 校验签名
     *
     * @param request
     * @param response
     * @return
     */
    private static void verifySignature(final YopRequest request, final YopResponse response) {
        String expectedSign = response.getSign();
        if (StringUtils.isBlank(expectedSign) || null == response.getStringResult()) {
            return;
        }

        String trimmedBizResult = response.getStringResult().replaceAll("[ \t\n]", "");
        StringBuilder sb = new StringBuilder(request.getAesSecretKey())
                .append(StringUtils.trimToEmpty(response.getState() + trimmedBizResult + response.getTs()))
                .append(request.getAesSecretKey());
        String calculatedSign = Digests.digest2Hex(sb.toString(), StringUtils.isBlank(request.getSignAlg()) ? YopConstants.ALG_SHA1 : request.getSignAlg());
        boolean valid = StringUtils.equalsIgnoreCase(expectedSign, calculatedSign);
        response.setValidSign(valid);
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
        String localSignature = Digests.digest2Hex(key + encryption + key, signatureAlg);
        //验签失败...
        if (!localSignature.equals(signature)) {
            return null;
        }
        return encryption;
    }
}
