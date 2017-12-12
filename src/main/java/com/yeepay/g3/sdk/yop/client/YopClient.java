package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.Digest;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.DateUtils;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.*;

/**
 * <pre>
 * 功能说明：易宝开放平台(YeepayOpenPlatform简称YOP)SDK客户端，简化用户发起请求及解析结果的处理，包括加解密
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopClient extends AbstractClient {

    protected static final Logger logger = Logger.getLogger(YopClient.class);

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String apiUri, YopRequest request) {
        String content = postForString(apiUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
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
    public static YopResponse get(String apiUri, YopRequest request) {
        String responseRawJson = getForString(apiUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(responseRawJson, YopResponse.class);
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
    public static YopResponse upload(String apiUri, YopRequest request) {
        String content = uploadForString(apiUri, request);
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
    public static String postForString(String apiUri, YopRequest request) {
        String serverUrl = richRequest(apiUri, request);
        sign(request);
        request.encoding();
        return getRestTemplate().postForObject(serverUrl, new HttpEntity<MultiValueMap<String, String>>(request.getParams(), request.headers), String.class);
    }

    /**
     * 发起get请求，以字符串返回
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 字符串形式的响应
     */
    public static String getForString(String apiUri, YopRequest request) {
        if (!request.headers.containsKey(Headers.YOP_REQUEST_ID)) {
            String requestId = UUID.randomUUID().toString();
            request.headers.add(Headers.YOP_REQUEST_ID, requestId);
        }

        String timestamp = DateUtils.formatCompressedIso8601Timestamp(System.currentTimeMillis());
        request.headers.add(Headers.YOP_DATE, timestamp);

        String serverUrl = buildURL(apiUri, request);
        return getRestTemplate().exchange(serverUrl, HttpMethod.GET, new HttpEntity(request.headers), String.class).getBody();
    }

    /**
     * 发起文件上传请求，以字符串返回
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 字符串形式的响应
     */
    public static String uploadForString(String apiUri, YopRequest request) {
        String serverUrl = richRequest(apiUri, request);

        MultiValueMap<String, String> original = request.getParams();
        MultiValueMap<String, Object> alternate = new LinkedMultiValueMap<String, Object>();
        List<String> uploadFiles = request.getParam("_file");
        if (null == uploadFiles || uploadFiles.size() == 0) {
            throw new RuntimeException("上传文件时参数_file不能为空!");
        }
        for (String uploadFile : uploadFiles) {
            try {
                alternate.add("_file", new UrlResource(new URI(uploadFile)));
            } catch (Exception e) {
                logger.debug("_file upload error.", e);
            }
        }

        sign(request);
        request.encoding();

        for (String key : original.keySet()) {
            alternate.put(key, new ArrayList<Object>(original.get(key)));
        }

        String content = getRestTemplate().postForObject(serverUrl, alternate, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    /**
     * 简单校验及请求签名
     */
    private static void sign(YopRequest request) {
        Assert.notNull(request.getSecretKey(), "secretKey must be specified");
        String signValue = sign(toSimpleMap(request.getParams()),
                request.getIgnoreSignParams(), request.getSecretKey(),
                request.getSignAlg());
        request.addParam(YopConstants.SIGN, signValue);

        //TODO why is here?
        request.removeParam(YopConstants.VERSION);
    }

    /**
     * 对paramValues进行签名，其中ignoreParamNames这些参数不参与签名
     *
     * @param paramValues
     * @param ignoreParamNames
     * @param secret
     * @return
     */
    private static String sign(Map<String, String> paramValues, List<String> ignoreParamNames, String secret, String algName) {
        algName = StringUtils.isBlank(algName) ? YopConstants.ALG_SHA1 : algName;

        StringBuilder sb = new StringBuilder();
        List<String> paramNames = new ArrayList<String>(paramValues.size());
        paramNames.addAll(paramValues.keySet());
        if (ignoreParamNames != null && ignoreParamNames.size() > 0) {
            for (String ignoreParamName : ignoreParamNames) {
                paramNames.remove(ignoreParamName);
            }
        }
        Collections.sort(paramNames);

        sb.append(secret);
        for (String paramName : paramNames) {
            if (StringUtils.isBlank(paramValues.get(paramName))) {
                continue;
            }
            sb.append(paramName).append(paramValues.get(paramName));
        }
        sb.append(secret);
        return Digest.digest(sb.toString(), algName);
    }

    private static Map<String, String> toSimpleMap(MultiValueMap<String, String> form) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, List<String>> entry : form.entrySet()) {
            map.put(entry.getKey(), listAsString(entry.getValue()));
        }
        return map;
    }

    /**
     * 数组、列表按值排序后逗号拼接
     *
     * @param list 参数列表
     * @return 拼接结果
     */
    private static String listAsString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Collections.sort(list);
        return StringUtils.join(list, ",");
    }

    private static void handleResult(YopRequest request, YopResponse response) {
        String stringResult = response.getStringResult();
        if (StringUtils.isNotBlank(stringResult)) {
            response.setResult(JacksonJsonMarshaller.unmarshal(stringResult, LinkedHashMap.class));
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
     * 帮助方法，构建get类型的完整请求路径
     *
     * @param methodOrUri
     * @param request
     * @return
     */
    private static String buildURL(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(methodOrUri, request);
        sign(request);
        request.encoding();
        serverUrl += serverUrl.contains("?") ? "&" : "?" + request.toQueryString();
        return serverUrl;
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
