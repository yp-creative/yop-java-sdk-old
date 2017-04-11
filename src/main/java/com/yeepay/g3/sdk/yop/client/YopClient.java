package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.BlowfishCipher;
import com.yeepay.g3.sdk.yop.encrypt.Digest;
import com.yeepay.g3.sdk.yop.encrypt.YopSignUtils;
import com.yeepay.g3.sdk.yop.enums.HttpMethodType;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.UrlResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

/**
 * <pre>
 * 功能说明：易宝开放平台(YeepayOpenPlatform简称YOP)SDK客户端，简化用户发起请求及解析结果的处理，包括加解密
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
@Deprecated
public class YopClient extends YopBaseClient {

    protected static final Logger logger = Logger.getLogger(YopClient.class);

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String methodOrUri, YopRequest request) {
        String content = postForString(methodOrUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        handleResult(request, response, content);
        return response;
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse get(String methodOrUri, YopRequest request) {
        String content = getForString(methodOrUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        handleResult(request, response, content);
        return response;
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse upload(String methodOrUri, YopRequest request) {
        String content = uploadForString(methodOrUri, request);
        YopResponse response = JacksonJsonMarshaller.unmarshal(content, YopResponse.class);
        handleResult(request, response, content);
        return response;
    }

    /**
     * 发起post请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String postForString(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri,
                request);
        signAndEncrypt(request);
        logger.info("signature:" + request.getParamValue(YopConstants.SIGN));
        request.setAbsoluteURL(serverUrl);
        request.encoding();

        String content = getRestTemplate(request).postForObject(serverUrl,
                request.getParams(), String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    /**
     * 发起get请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String getForString(String methodOrUri, YopRequest request) {
        String serverUrl = buildURL(methodOrUri, request);
        request.setAbsoluteURL(serverUrl);
        String content = getRestTemplate(request).getForObject(serverUrl, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    /**
     * 发起文件上传请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String uploadForString(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri, request);

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

        signAndEncrypt(request);
        request.setAbsoluteURL(serverUrl);
        request.encoding();

        for (String key : original.keySet()) {
            alternate.put(key, new ArrayList<Object>(original.get(key)));
        }

        String content = getRestTemplate(request).postForObject(serverUrl, alternate, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    /**
     * 简单校验及请求签名
     */
    public static void signAndEncrypt(YopRequest request) {
        Assert.notNull(request.getMethod(), "method must be specified");
        Assert.notNull(request.getSecretKey(), "secretKey must be specified");
        String appKey = request.getParamValue(YopConstants.APP_KEY);
        if (StringUtils.isBlank(appKey)) {
            appKey = StringUtils.trimToNull(request
                    .getParamValue(YopConstants.CUSTOMER_NO));
        }
        Assert.notNull(appKey, "appKey 与 customerNo 不能同时为空");
        String signValue = YopSignUtils.sign(toSimpleMap(request.getParams()),
                request.getIgnoreSignParams(), request.getSecretKey(),
                request.getSignAlg());
        request.addParam(YopConstants.SIGN, signValue);
        if (request.isRest()) {
            request.removeParam(YopConstants.METHOD);
            request.removeParam(YopConstants.VERSION);
        }

        // 签名之后再加密
        if (request.isEncrypt()) {
            try {
                encrypt(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 请求加密，使用AES算法，要求secret为正常的AESkey
     *
     * @throws Exception
     */
    protected static void encrypt(YopRequest request) throws Exception {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        MultiValueMap<String, String> params = request.getParams();
        List<String> keys = new ArrayList<String>(params.keySet());
        for (String key : keys) {
            if (YopConstants.isProtectedKey(key)) {
                continue;
            }
            List<String> values = params.remove(key);
            if (values == null || values.isEmpty()) {
                continue;
            }
            for (String v : values) {
                if (first) {
                    first = false;
                } else {
                    builder.append("&");
                }
                // 避免解密后解析异常，此处需进行encode（此逻辑在整个request做encoding前）
                builder.append(key).append("=")
                        .append(URLEncoder.encode(v, YopConstants.ENCODING));
            }
        }
        String encryptBody = builder.toString();
        if (StringUtils.isBlank(encryptBody)) {
            // 没有需加密的参数，则只标识响应需加密
            request.addParam(YopConstants.ENCRYPT, true);
        } else {
            if (StringUtils.isNotBlank(request
                    .getParamValue(YopConstants.APP_KEY))) {
                // 开放应用使用AES加密
                String encrypt = AESEncrypter.encrypt(encryptBody,
                        request.getSecretKey());
                request.addParam(YopConstants.ENCRYPT, encrypt);
            } else {
                // 商户身份调用使用Blowfish加密
                String encrypt = BlowfishCipher.encrypt(encryptBody,
                        request.getSecretKey());
                request.addParam(YopConstants.ENCRYPT, encrypt);
            }
        }
    }

    protected static String decrypt(YopRequest request, String strResult) {
        if (request.isEncrypt() && StringUtils.isNotBlank(strResult)) {
            if (StringUtils.isNotBlank(request
                    .getParamValue(YopConstants.APP_KEY))) {
                strResult = AESEncrypter.decrypt(strResult,
                        request.getSecretKey());
            } else {
                strResult = BlowfishCipher.decrypt(strResult,
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

    protected static void handleResult(YopRequest request,
                                       YopResponse response, String content) {
        String ziped = StringUtils.EMPTY;
        if (response.isSuccess()) {
            String strResult = getBizResult(content);
            ziped = strResult.replaceAll("[ \t\n]", "");
            // 先解密，极端情况可能业务正常，但返回前处理（如加密）出错，所以要判断是否有error
            if (StringUtils.isNotBlank(strResult)
                    && response.getError() == null) {
                if (request.isEncrypt()) {
                    String decryptResult = decrypt(request, strResult.trim());
                    response.setStringResult(decryptResult);
                    response.setResult(decryptResult);
                    ziped = decryptResult.replaceAll("[ \t\n]", "");
                } else {
                    response.setStringResult(strResult);
                }
            }
        }
        // 再验签
        if (request.isSignRet() && StringUtils.isNotBlank(response.getSign())) {
            String signStr = response.getState() + ziped + response.getTs();
            response.setValidSign(YopSignUtils.isValidResult(signStr,
                    request.getSecretKey(), request.getSignAlg(),
                    response.getSign()));
        } else {
            response.setValidSign(true);
        }
    }

    /**
     * 帮助方法，构建get类型的完整请求路径
     *
     * @param methodOrUri
     * @param request
     * @return
     */
    public static String buildURL(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(HttpMethodType.GET, methodOrUri, request);
        signAndEncrypt(request);
        request.encoding();
        serverUrl += serverUrl.contains("?") ? "&" : "?" + request.toQueryString();
        return serverUrl;
    }

    /**
     * 帮助方法，对request签名并返回签名结果
     *
     * @param methodOrUri
     * @param request
     * @return 签名结果
     */
    public static String getSign(String methodOrUri, YopRequest request) {
        richRequest(HttpMethodType.GET, methodOrUri, request);
        signAndEncrypt(request);
        return request.getParamValue(YopConstants.SIGN);
    }

    public static String acceptNotificationAsJson(String key, String response) {
        return validateAndDecryptNotification(key, response);
    }

    public static Map acceptNotificationAsMap(String key, String response) {
        String s = acceptNotificationAsJson(key, response);
//        return s == null ? null : jm.unmarshal(acceptNotificationAsJson(key, response), Map.class);
        return s == null ? null : JsonUtils.fromJsonString(acceptNotificationAsJson(key, response), Map.class);
    }

    private static String validateAndDecryptNotification(String key, String response) {
//        Map map = jm.unmarshal(response, Map.class);
        Map map = JsonUtils.fromJsonString(response, Map.class);
        //是否加密
        boolean doEncryption = Boolean.valueOf(map.get("doEncryption").toString());
        //内容
        String encryption = map.get("encryption").toString();
        //签名
        String signature = map.get("signature").toString();

        String encryptionAlg = map.get("encryptionAlg").toString();

        String signatureAlg = map.get("signatureAlg").toString();

        //如果加密，解密
        if (doEncryption) {
            encryption = encryptionAlg.equals("BLOWFISH") ? BlowfishCipher.decrypt(encryption, key) : AESEncrypter
                    .decrypt(encryption, key);
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
