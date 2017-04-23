package com.yeepay.g3.sdk.yop.encrypt;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 对paramValues进行签名，其中ignoreParamNames这些参数不参与签名
     *
     * @param paramValues
     * @param ignoreParamNames
     * @param secret
     * @return
     */
    public static String sign(Map<String, String> paramValues, List<String> ignoreParamNames, String secret, String algName) {
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

    /**
     * 对业务结果签名进行校验
     */
    public static boolean isValidResult(String result, String secret, String algName, String sign) {
        algName = StringUtils.isBlank(algName) ? YopConstants.ALG_SHA1 : algName;

        StringBuilder sb = new StringBuilder();
        sb.append(secret);
        sb.append(StringUtils.trimToEmpty(result));
        sb.append(secret);
        String newSign = Digest.digest(sb.toString(), algName);
        return StringUtils.equalsIgnoreCase(sign, newSign);
    }
}
