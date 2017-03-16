package com.yeepay.g3.sdk.yop.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.g3.facade.yop.ca.dto.DigitalSignatureDTO;
import com.yeepay.g3.facade.yop.ca.enums.CertTypeEnum;
import com.yeepay.g3.facade.yop.ca.enums.DigestAlgEnum;
import com.yeepay.g3.frame.yop.ca.DigitalEnvelopeUtils;
import com.yeepay.g3.frame.yop.ca.rsa.RSAKeyUtils;
import com.yeepay.g3.frame.yop.ca.utils.Exceptions;
import com.yeepay.g3.sdk.yop.enums.HttpMethodType;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpUtils;
import com.yeepay.g3.sdk.yop.unmarshaller.YopMarshallerUtils;
import com.yeepay.g3.sdk.yop.utils.DateUtils;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <pre>
 * 功能说明：易宝开放平台(YeepayOpenPlatform简称YOP)SDK客户端，简化用户发起请求及解析结果的处理，包括加解密
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopClient3 extends YopBaseClient {

    protected static final Logger logger = Logger.getLogger(YopClient3.class);

    private static final Set<String> defaultHeadersToSign = Sets.newHashSet();
    private static final Joiner headerJoiner = Joiner.on('\n');
    private static final Joiner signedHeaderStringJoiner = Joiner.on(';');

    private static final String EXPIRED_SECONDS = "1800";

    static {
        defaultHeadersToSign.add(Headers.HOST.toLowerCase());
        defaultHeadersToSign.add(Headers.CONTENT_LENGTH.toLowerCase());
        defaultHeadersToSign.add(Headers.CONTENT_TYPE.toLowerCase());
        defaultHeadersToSign.add(Headers.CONTENT_MD5.toLowerCase());
    }

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse postRsa(String methodOrUri, YopRequest request) {
        String content = postRsaString(methodOrUri, request);
        YopResponse response = YopMarshallerUtils.unmarshal(content,
                request.getFormat(), YopResponse.class);
        handleRsaResult(request, response, content);
        return response;
    }

    /**
     * 发起post请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String postRsaString(String methodOrUri, YopRequest request) {
        logger.debug(request.toQueryString());
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri, request);
        request.setAbsoluteURL(serverUrl);
        String content = getRestTemplate(request).postForObject(serverUrl, signAndEncrypt(methodOrUri, request), String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    private static HttpEntity<MultiValueMap<String, String>> signAndEncrypt(String methodOrUri, YopRequest request) {
        String appKey = request.getAppKey();
        String timestamp = DateUtils.formatCompressedIso8601Timestamp(new Date().getTime());
        InternalConfig internalConfig = InternalConfig.Factory.getInternalConfig();
        String protocolVersion = internalConfig.getProtocolVersion();

//        authorization  yop-auth-v2/openSmsApi/2016-02-25T08:57:48Z/1800/host/a57365cb4bf6cd83c91dfae214c1404aa0cc74f2ade95f121530fcb9c91f3c9d

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        String requestId = UUID.randomUUID().toString();
        headers.add("x-yop-request-id", requestId);
        headers.add("x-yop-date", timestamp);

        String authString = protocolVersion + "/" + appKey + "/" + timestamp + "/" + EXPIRED_SECONDS;

        Set<String> headersToSignSet = new HashSet<String>();
        headersToSignSet.add("x-yop-request-id");
        headersToSignSet.add("x-yop-date");

        if (StringUtils.isBlank(request.getCustomerNo())) {
            headers.add("x-yop-appkey", appKey);
            headersToSignSet.add("x-yop-appkey");
        } else {
            headers.add("x-yop-customerid", appKey);
            headersToSignSet.add("x-yop-customerid");
        }

        // Formatting the URL with signing protocol.
        String canonicalURI = HttpUtils.getCanonicalURIPath(methodOrUri);
        // Formatting the query string with signing protocol.
        String canonicalQueryString = getCanonicalQueryString(request.getParams().toSingleValueMap(), true);
        // Sorted the headers should be signed from the request.
        SortedMap<String, String> headersToSign = getHeadersToSign(headers, headersToSignSet);
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = getCanonicalHeaders(headersToSign);
        String signedHeaders = "";
        if (headersToSignSet != null) {
            signedHeaders = signedHeaderStringJoiner.join(headersToSign.keySet());
            signedHeaders = signedHeaders.trim().toLowerCase();
        }

        String canonicalRequest = authString + "\n" + "POST" + "\n" + canonicalURI + "\n" + canonicalQueryString + "\n" + canonicalHeader;

        // Signing the canonical request using key with sha-256 algorithm.

        PrivateKey isvPrivateKey;
        if (StringUtils.isNotBlank(request.getSecretKey())) {
            try {
                isvPrivateKey = RSAKeyUtils.string2PrivateKey(request.getSecretKey());
            } catch (NoSuchAlgorithmException e) {
                throw Exceptions.unchecked(e);
            } catch (InvalidKeySpecException e) {
                throw Exceptions.unchecked(e);
            }
        } else {
            isvPrivateKey = internalConfig.getISVPrivateKey(CertTypeEnum.RSA2048);
        }
        if (null == isvPrivateKey) {
            throw new YopClientException("Can't init ISV private key!");
        }

        DigitalSignatureDTO digitalSignatureDTO = new DigitalSignatureDTO();
        digitalSignatureDTO.setPlainText(canonicalRequest);
        digitalSignatureDTO.setCertType(CertTypeEnum.RSA2048);
        digitalSignatureDTO.setDigestAlg(DigestAlgEnum.SHA256);
        digitalSignatureDTO = DigitalEnvelopeUtils.sign(digitalSignatureDTO, isvPrivateKey);

        headers.add("Authorization", "YOP-RSA2048-SHA256 " + protocolVersion + "/" + appKey + "/" + timestamp + "/" + EXPIRED_SECONDS + "/" + signedHeaders + "/" + digitalSignatureDTO.getSignature());

        request.encoding();

        return new HttpEntity<MultiValueMap<String, String>>(request.getParams(), headers);
    }

    /**
     * 上传文件
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 响应对象
     */
    public static YopResponse uploadRsa(String methodOrUri, YopRequest request) {
        String content = uploadRsaForString(methodOrUri, request);
        YopResponse response = YopMarshallerUtils.unmarshal(content, request.getFormat(), YopResponse.class);
        handleRsaResult(request, response, content);
        return response;
    }

    /**
     * 发起文件上传请求，以字符串返回
     *
     * @param methodOrUri 目标地址或命名模式的method
     * @param request     客户端请求对象
     * @return 字符串形式的响应
     */
    public static String uploadRsaForString(String methodOrUri, YopRequest request) {
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri, request);
        request.setAbsoluteURL(serverUrl);

        MultiValueMap<String, String> original = request.getParams();
        MultiValueMap<String, Object> alternate = new LinkedMultiValueMap<String, Object>();
        List<String> uploadFiles = request.getParam("_file");
        request.removeParam("_file");
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

        HttpEntity<MultiValueMap<String, String>> originalHttpEntity = signAndEncrypt(methodOrUri, request);

        for (String key : originalHttpEntity.getBody().keySet()) {
            alternate.put(key, new ArrayList<Object>(original.get(key)));
        }

        HttpEntity<MultiValueMap<String, Object>> alternateHttpEntity = new HttpEntity<MultiValueMap<String, Object>>(alternate, originalHttpEntity.getHeaders());
        String content = getRestTemplate(request).postForObject(serverUrl, alternateHttpEntity, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("response:\n" + content);
        }
        return content;
    }

    private static final Joiner queryStringJoiner = Joiner.on('&');

    public static String getCanonicalQueryString(Map<String, String> parameters, boolean forSignature) {
        if (parameters.isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");

            if (forSignature && Headers.AUTHORIZATION.equalsIgnoreCase(key)) {
                continue;
            }

            parameterStrings.add(HttpUtils.normalize(key) + '=' + HttpUtils.normalize(entry.getValue()));
        }
        Collections.sort(parameterStrings);

        return queryStringJoiner.join(parameterStrings);
    }

    protected static void handleRsaResult(YopRequest request,
                                          YopResponse response, String content) {
        response.setFormat(request.getFormat());
        String ziped = StringUtils.EMPTY;
        if (response.isSuccess()) {
            String strResult = getBizResult(content, request.getFormat());
            ziped = strResult.replaceAll("[ \t\n]", "");
            // 先解密，极端情况可能业务正常，但返回前处理（如加密）出错，所以要判断是否有error
            if (StringUtils.isNotBlank(strResult)
                    && response.getError() == null) {
                response.setStringResult(strResult);
            }
        }
        // 再验签
        String signStr = ziped;
        isValidResult(signStr, response.getSign());
    }

    /**
     * 对业务结果签名进行校验
     */
    public static boolean isValidResult(String result, String sign) {
        if (StringUtils.isBlank(sign)) {
            return true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.trimToEmpty(result));

        DigitalSignatureDTO digitalSignatureDTO = new DigitalSignatureDTO();
        digitalSignatureDTO.setCertType(CertTypeEnum.RSA2048);
        digitalSignatureDTO.setSignature(sign);
        digitalSignatureDTO.setPlainText(sb.toString());

        InternalConfig internalConfig = InternalConfig.Factory.getInternalConfig();
        DigitalEnvelopeUtils.verify(digitalSignatureDTO, internalConfig.getYopPublicKey(CertTypeEnum.RSA2048));
        return true;
    }

    private static String getCanonicalHeaders(SortedMap<String, String> headers) {
        if (headers.isEmpty()) {
            return "";
        }

        List<String> headerStrings = Lists.newArrayList();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }
            headerStrings.add(HttpUtils.normalize(key.trim().toLowerCase()) + ':' + HttpUtils.normalize(value.trim()));
        }
        Collections.sort(headerStrings);

        return headerJoiner.join(headerStrings);
    }

    private static SortedMap<String, String> getHeadersToSign(MultiValueMap<String, String> headers, Set<String> headersToSign) {
        SortedMap<String, String> ret = Maps.newTreeMap();
        if (headersToSign != null) {
            Set<String> tempSet = Sets.newHashSet();
            for (String header : headersToSign) {
                tempSet.add(header.trim().toLowerCase());
            }
            headersToSign = tempSet;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if ((headersToSign == null && isDefaultHeaderToSign(key))
                        || (headersToSign != null && headersToSign.contains(key.toLowerCase())
                        && !Headers.AUTHORIZATION.equalsIgnoreCase(key))) {
                    ret.put(key, entry.getValue().get(0));
                }
            }
        }
        return ret;
    }

    private static boolean isDefaultHeaderToSign(String header) {
        header = header.trim().toLowerCase();
        return header.startsWith(Headers.YOP_PREFIX) || defaultHeadersToSign.contains(header);
    }
}
