package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * <pre>
 * 每个请求对应一个ClientRequest对象
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopRequest {

    private Logger logger = Logger.getLogger(getClass());

    private String locale = "zh_CN";

    private String signAlg = YopConstants.ALG_SHA1;

    private Multimap<String, String> paramMap = ArrayListMultimap.create();

    private Multimap<String, Object> multiportFiles = ArrayListMultimap.create();

    private Map<String, String> headers = new HashMap<String, String>();

    private List<String> ignoreSignParams = new ArrayList<String>(Arrays.asList(YopConstants.SIGN));

    /**
     * 可支持不同请求使用不同的appKey及secretKey
     */
    private String appKey;

    /**
     * 可支持不同请求使用不同的appKey及secretKey,secretKey只用于本地签名，不会被提交
     */
    private String secretKey;

    /**
     * 可支持不同请求使用不同的appKey及secretKey、serverRoot,secretKey只用于本地签名，不会被提交
     */
    private String serverRoot;

    public YopRequest() {
        this(InternalConfig.APP_KEY, InternalConfig.SECRET_KEY, InternalConfig.SERVER_ROOT);
    }

    public YopRequest(String serverRoot) {
        this(InternalConfig.APP_KEY, InternalConfig.SECRET_KEY, serverRoot);
    }

    /**
     * 同一个工程内部可支持多个开放应用发起调用
     */
    public YopRequest(String appKey, String secretKey) {
        this(appKey, secretKey, InternalConfig.SERVER_ROOT);
    }

    /**
     * 同一个工程内部可支持多个开放应用发起调用，且支持调不同的服务器
     */
    public YopRequest(String appKey, String secretKey, String serverRoot) {
        notNull(appKey, "必须指定 appKey");
//        notNull(secretKey, "必须指定 secretKey");
        notNull(serverRoot, "必须指定 serverRoot");
        this.appKey = appKey;
        this.secretKey = secretKey;

        if (StringUtils.endsWith(serverRoot, "/")) {
            this.serverRoot = StringUtils.substring(serverRoot, 0, -1);
        } else {
            this.serverRoot = serverRoot;
        }

        headers.put(Headers.USER_AGENT, YopConstants.USER_AGENT);

        paramMap.put(YopConstants.APP_KEY, this.appKey);
        paramMap.put(YopConstants.LOCALE, locale);
        paramMap.put(YopConstants.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
    }

    public YopRequest setParam(String paramName, Object paramValue) {
        removeParam(paramName);
        addParam(paramName, paramValue, false);
        return this;
    }

    public YopRequest addParam(String paramName, Object paramValue) {
        addParam(paramName, paramValue, false);
        return this;
    }

    /**
     * 添加参数
     *
     * @param paramName  参数名
     * @param paramValue 参数值：如果为集合或数组类型，则自动拆解，最终想作为数组提交到服务端
     * @param ignoreSign 是否忽略签名
     * @return
     */
    public YopRequest addParam(String paramName, Object paramValue, boolean ignoreSign) {
        Assert.hasText(paramName, "参数名不能为空");
        if (paramValue == null || ((paramValue instanceof String) && StringUtils.isBlank((String) paramValue))
                || ((paramValue instanceof Collection<?>) && ((Collection<?>) paramValue).isEmpty())) {
            logger.warn("param " + paramName + "is null or empty，ignore it");
            return this;
        }

        // file
        if (StringUtils.equals("_file", paramName)) {
            this.addFile(paramValue);
            return this;
        }

        if (YopConstants.isProtectedKey(paramName)) {
            paramMap.put(paramName, paramValue.toString());
            return this;
        }
        if (paramValue instanceof Collection<?>) {
            // 集合类
            for (Object o : (Collection<?>) paramValue) {
                if (o != null) {
                    paramMap.put(paramName, o.toString());
                }
            }
        } else if (paramValue.getClass().isArray()) {
            // 数组
            int len = Array.getLength(paramValue);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(paramValue, i);
                if (o != null) {
                    paramMap.put(paramName, o.toString());
                }
            }
        } else {
            paramMap.put(paramName, paramValue.toString());
        }

        if (ignoreSign) {
            ignoreSignParams.add(paramName);
        }
        return this;
    }

    public List<String> getParam(String key) {
        return (List<String>) paramMap.get(key);
    }

    public String getParamValue(String key) {
        return StringUtils.join(paramMap.get(key), ",");
    }

    public String removeParam(String key) {
        return StringUtils.join(paramMap.removeAll(key), ",");
    }

    public Multimap<String, String> getParams() {
        return paramMap;
    }

    public YopRequest addFile(Object file) {
        addFile("_file", file);
        return this;
    }

    public YopRequest addFile(String paramName, Object file) {
        if (file instanceof String || file instanceof File || file instanceof InputStream) {
            multiportFiles.put(paramName, file);
        } else {
            throw new YopClientException("Unsupported file object.");
        }
        return this;
    }

    public Multimap<String, Object> getMultiportFiles() {
        return multiportFiles;
    }

    public boolean hasFiles() {
        return null != multiportFiles && multiportFiles.size() > 0;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * customize header,but none system reserved headers
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setRequestId(String requestId) {
        headers.put(Headers.YOP_REQUEST_ID, requestId);
    }

    public void setRequestSource(String source) {
        headers.put(Headers.YOP_REQUEST_SOURCE, source);
    }

    public List<String> getIgnoreSignParams() {
        return ignoreSignParams;
    }

    public void setLocale(String locale) {
        this.locale = locale;
        paramMap.put(YopConstants.LOCALE, this.locale);
    }

    public String getLocale() {
        return locale;
    }

    public String getSignAlg() {
        return signAlg;
    }

    public void setSignAlg(String signAlg) {
        this.signAlg = signAlg;
    }

    /**
     * 该设置已无效，不再加密，安全传输由https保证
     *
     * @param encrypt
     */
    @Deprecated
    public void setEncrypt(boolean encrypt) {

    }

    /**
     * 该设置已无效，签名是必须的
     *
     * @param signRet
     */
    @Deprecated
    public void setSignRet(boolean signRet) {

    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    /**
     * 将参数转换成k=v拼接的形式
     *
     * @return
     */
    public String toQueryString() {
        StringBuilder builder = new StringBuilder();
        for (String key : this.paramMap.keySet()) {
            Collection<String> values = this.paramMap.get(key);
            for (String value : values) {
                builder.append(builder.length() == 0 ? "" : "&");
                builder.append(key);
                builder.append("=");
                builder.append(value);
            }
        }
        return builder.toString();
    }
}
