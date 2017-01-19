package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.frame.yop.ca.utils.Encodes;
import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.BlowfishEncrypter;
import com.yeepay.g3.sdk.yop.enums.FormatType;
import com.yeepay.g3.sdk.yop.enums.HttpMethodType;
import com.yeepay.g3.sdk.yop.unmarshaller.YopMarshallerUtils;
import com.yeepay.g3.sdk.yop.utils.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 功能说明：易宝开放平台(YeepayOpenPlatform简称YOP)SDK客户端，简化用户发起请求及解析结果的处理，包括加解密
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopClient2 {

    protected static final Logger logger = Logger.getLogger(YopClient2.class);

    protected static RestTemplate restTemplate = new YopRestTemplate();

    protected static Map<String, List<String>> uriTemplateCache = new HashMap<String, List<String>>();

//    private static JacksonJsonMarshaller jm = new JacksonJsonMarshaller();

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse postBasic(String methodOrUri, YopRequest request) {
        String content = postBasicForString(methodOrUri, request);
        YopResponse response = YopMarshallerUtils.unmarshal(content, request.getFormat(), YopResponse.class);
        return response;
    }

    /**
     * 发起post请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String postBasicForString(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri, request);
        logger.info("signature:" + request.getParamValue(YopConstants.SIGN));
        request.setAbsoluteURL(serverUrl);
        request.encoding();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

        String authorizationHeader="";
        try {
            authorizationHeader= Encodes.encodeBase64(("yop-boss:"+request.getSecretKey()).getBytes("utf-8")).trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        headers.add("Authorization", "Basic " + authorizationHeader);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(request.getParams(), headers);

        String content = getRestTemplate(request).postForObject(serverUrl, httpEntity, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    private static RestTemplate getRestTemplate(YopRequest request) {
        if (null != request.getConnectTimeout() || null != request.getReadTimeout()) {
            int connectTimeout = null != request.getConnectTimeout() ? request.getConnectTimeout().intValue() : YopConfig.getConnectTimeout();
            int readTimeout = null != request.getReadTimeout() ? request.getReadTimeout().intValue() : YopConfig.getReadTimeout();
            return new YopRestTemplate(connectTimeout, readTimeout);
        } else {
            return restTemplate;
        }
    }

    protected static String decrypt(YopRequest request, String strResult) {
        if (request.isEncrypt() && StringUtils.isNotBlank(strResult)) {
            if (StringUtils.isNotBlank(request
                    .getParamValue(YopConstants.APP_KEY))) {
                strResult = AESEncrypter.decrypt(strResult,
                        request.getSecretKey());
            } else {
                strResult = BlowfishEncrypter.decrypt(strResult,
                        request.getSecretKey());
            }
        }
        return strResult;
    }

    protected static Map<String, String> toSimpleMap(
            MultiValueMap<String, String> form) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, List<String>> entry : form.entrySet()) {
            map.put(entry.getKey(), listAsString(entry.getValue()));
        }
        return map;
    }

    /**
     * 数组、列表按值排序后逗号拼接
     *
     * @param list
     * @return
     */
    protected static String listAsString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Collections.sort(list);
        return StringUtils.join(list, ",");
    }

    /**
     * 自动补全请求
     */
    protected static String richRequest(HttpMethodType type,
                                        String methodOrUri, YopRequest request) {
        Assert.notNull(methodOrUri, "method name or rest uri");
        if (methodOrUri.startsWith(request.getServerRoot())) {
            methodOrUri = methodOrUri.substring(request.getServerRoot()
                    .length() + 1);
        }
        boolean isRest = methodOrUri.startsWith("/rest/");
        request.setRest(isRest);
        String serverUrl = request.getServerRoot();
        if (isRest) {
            methodOrUri = mergeTplUri(methodOrUri, request);
            serverUrl += methodOrUri;
            String version = StringUtils.substringBetween(methodOrUri,
                    "/rest/v", "/");
            if (StringUtils.isNotBlank(version)) {
                request.setVersion(version);
            }
        } else {
            serverUrl += "/command?" + YopConstants.METHOD + "=" + methodOrUri;
        }
        request.setMethod(methodOrUri);
        return serverUrl;
    }

    /**
     * 从完整返回结果中获取业务结果，主要用于验证返回结果签名
     */
    private static String getBizResult(String content, FormatType format) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        switch (format) {
            case json:
                String jsonStr = StringUtils.substringAfter(content,
                        "\"result\" : ");
                jsonStr = StringUtils.substringBeforeLast(jsonStr, "\"ts\"");
                // 去除逗号
                jsonStr = StringUtils.substringBeforeLast(jsonStr, ",");
                return jsonStr;
            default:
                String xmlStr = StringUtils.substringAfter(content, "</state>");
                xmlStr = StringUtils.substringBeforeLast(xmlStr, "<ts>");
                return xmlStr;
        }
    }


    /**
     * 模板URL自动补全参数
     *
     * @param tplUri
     * @param request
     * @return
     */
    protected static String mergeTplUri(String tplUri, YopRequest request) {
        String uri = tplUri;
        if (tplUri.indexOf("{") < 0) {
            return uri;
        }
        List<String> dynaParamNames = uriTemplateCache.get(tplUri);
        if (dynaParamNames == null) {
            dynaParamNames = new LinkedList<String>();
            Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
            Matcher matcher = pattern.matcher(tplUri);
            while (matcher.find()) {
                dynaParamNames.add(matcher.group(1));
            }
            uriTemplateCache.put(tplUri, dynaParamNames);
        }
        for (String dynaParamName : dynaParamNames) {
            String value = request.removeParam(dynaParamName);
            Assert.notNull(value, dynaParamName + " must be specified");
            uri = uri.replace("{" + dynaParamName + "}", value);
        }
        return uri;
    }
}
