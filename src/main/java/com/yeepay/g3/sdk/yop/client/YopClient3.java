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
import com.yeepay.g3.sdk.yop.enums.FormatType;
import com.yeepay.g3.sdk.yop.enums.HttpMethodType;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpUtils;
import com.yeepay.g3.sdk.yop.unmarshaller.YopMarshallerUtils;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.DateUtils;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <pre>
 * 功能说明：易宝开放平台(YeepayOpenPlatform简称YOP)SDK客户端，简化用户发起请求及解析结果的处理，包括加解密
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopClient3 {

    protected static final Logger logger = Logger.getLogger(YopClient3.class);

    protected static RestTemplate restTemplate = new YopRestTemplate();

    protected static Map<String, List<String>> uriTemplateCache = new HashMap<String, List<String>>();

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
        String serverUrl = richRequest(HttpMethodType.POST, methodOrUri,
                request);
        logger.info("signature:" + request.getParamValue(YopConstants.SIGN));
        request.setAbsoluteURL(serverUrl);

        String appKey = request.getAppKey();
        String timestamp = DateUtils.formatAlternateIso8601Date(new Date());
        InternalConfig internalConfig = InternalConfig.Factory.getInternalConfig();
        String protocolVersion = internalConfig.getProtocolVersion();

//        authorization  yop-auth-v2/openSmsApi/2016-02-25T08:57:48Z/1800/host/a57365cb4bf6cd83c91dfae214c1404aa0cc74f2ade95f121530fcb9c91f3c9d

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        String requestId = UUID.randomUUID().toString();
        headers.add("x-yop-request-id", requestId);
        headers.add("x-yop-date", timestamp);

        String authString = protocolVersion + "/" + appKey + "/"
                + timestamp + "/" + EXPIRED_SECONDS;

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

        PrivateKey isvPrivateKey = null;
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

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(request.getParams(), headers);

        String content = getRestTemplate(request).postForObject(serverUrl, httpEntity, String.class);
        if (logger.isDebugEnabled()) {
            logger.debug("requestId:" + requestId + ", response:\n" + content);
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

            parameterStrings.add(normalize(key) + '=' + normalize(entry.getValue()));
        }
        Collections.sort(parameterStrings);

        return queryStringJoiner.join(parameterStrings);
    }


    private static BitSet URI_UNRESERVED_CHARACTERS = new BitSet();
    private static String[] PERCENT_ENCODED_STRINGS = new String[256];

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        URI_UNRESERVED_CHARACTERS.set('-');
        URI_UNRESERVED_CHARACTERS.set('.');
        URI_UNRESERVED_CHARACTERS.set('_');
        URI_UNRESERVED_CHARACTERS.set('~');

        for (int i = 0; i < PERCENT_ENCODED_STRINGS.length; ++i) {
            PERCENT_ENCODED_STRINGS[i] = String.format("%%%02X", i);
        }
    }

    public static String normalize(String value) {
        try {
            StringBuilder builder = new StringBuilder();
            for (byte b : value.getBytes(YopConstants.ENCODING)) {
                int bl = b & 0xFF;
                if (URI_UNRESERVED_CHARACTERS.get(bl)) {
                    builder.append((char) b);
                } else {
                    builder.append(PERCENT_ENCODED_STRINGS[bl]);
                }
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
