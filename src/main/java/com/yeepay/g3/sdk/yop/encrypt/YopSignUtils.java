package com.yeepay.g3.sdk.yop.encrypt;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopSignUtils {

    private static final Logger logger = Logger.getLogger(YopSignUtils.class);

    /**
     * 使用<code>secret</code>对paramValues按以下算法进行签名： <br/>
     * uppercase(hex(sha1(secretkey1value1key2value2...secret))
     *
     * @param paramValues 参数列表
     * @param secret
     * @return
     */
    public static String sign(Map<String, String> paramValues, String secret) {
        return sign(paramValues, null, secret, YopConstants.ALG_SHA);
    }

    /**
     * 使用<code>secret</code>对paramValues按以下算法进行签名： <br/>
     * uppercase(hex(sha1(secretkey1value1key2value2...secret))
     *
     * @param paramValues 参数列表
     * @param secret
     * @return
     */
    public static String sign(Map<String, String> paramValues, String secret,
                              String algName) {
        return sign(paramValues, null, secret, algName);
    }

    /**
     * 对paramValues进行签名，其中ignoreParamNames这些参数不参与签名
     *
     * @param paramValues
     * @param ignoreParamNames
     * @param secret
     * @return
     */
    public static String sign(Map<String, String> paramValues,
                              List<String> ignoreParamNames, String secret, String algName) {
        Assert.notNull(paramValues);
        Assert.notNull(secret);
        if (StringUtils.isBlank(algName)) {
            algName = YopConstants.ALG_SHA1;
        }
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

    /**
     * 对业务结果签名进行校验
     */
    public static boolean isValidResult(String result, String secret,
                                        String algName, String sign) {
        Assert.notNull(secret);
        Assert.notNull(sign);
        if (StringUtils.isBlank(algName)) {
            algName = YopConstants.ALG_SHA;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(secret);
        sb.append(StringUtils.trimToEmpty(result));
        sb.append(secret);
        String newSign = Digest.digest(sb.toString(), algName);
        if (logger.isDebugEnabled()) {
            logger.debug("本地签名：" + newSign + " | 服务端签名：" + sign);
        }
        return StringUtils.equalsIgnoreCase(sign, newSign);
    }

    /**
     * 对业务结果签名进行校验
     */
    public static boolean isValidResult(String result, List<String> secrets,
                                        String algName, String sign) {
        for (String secret : secrets) {
            if (isValidResult(result, secret, algName, sign)) {
                return true;
            }
        }
        return false;
    }
}
