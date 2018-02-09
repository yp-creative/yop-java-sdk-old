package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yeepay.g3.sdk.yop.YopServiceException;
import com.yeepay.g3.sdk.yop.config.AppSDKConfig;
import com.yeepay.g3.sdk.yop.config.AppSDKConfigSupport;
import com.yeepay.g3.sdk.yop.config.ConfigUtils;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.utils.Assert;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

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
     * app对应的sdkConfig
     */
    private final AppSDKConfig appSDKConfig;

    /**
     * 可支持不同请求使用不同的appKey及secretKey,secretKey只用于本地签名，不会被提交
     */
    private final String secretKey;

    public YopRequest() {
        this.appSDKConfig = AppSDKConfigSupport.getDefaultAppSDKConfig();
        if (this.appSDKConfig == null) {
            throw new YopServiceException("Default SDKConfig not found.");
        }
        this.secretKey = null;
        init();
    }

    public YopRequest(String appKey) {
        Validate.notBlank(appKey, "AppKey is blank.");
        this.appSDKConfig = AppSDKConfigSupport.getConfig(appKey);
        if (this.appSDKConfig == null) {
            throw new YopServiceException("SDKConfig for appKey:" + appKey + " not found.");
        }
        this.secretKey = null;
        init();
    }

    /**
     * 同一个工程内部可支持多个开放应用发起调用
     */
    public YopRequest(String appKey, String secretKey) {
        Validate.notBlank(appKey, "AppKey is blank.");
        Validate.notBlank(secretKey, "SecretKey is blank.");

        this.appSDKConfig = new AppSDKConfig();
        this.appSDKConfig.setAppKey(appKey);
        AppSDKConfig defaultAppSDKConfig = AppSDKConfigSupport.getDefaultAppSDKConfig();
        if (defaultAppSDKConfig == null) {
            this.appSDKConfig.setServerRoot(InternalConfig.SERVER_ROOT);
            this.appSDKConfig.setYopPublicKey(ConfigUtils.getDefaultYopPublicKey());
        } else {
            this.appSDKConfig.setServerRoot(defaultAppSDKConfig.getServerRoot());
            this.appSDKConfig.setYopPublicKey(defaultAppSDKConfig.getYopPublicKey());
        }
        this.secretKey = secretKey;
        init();
    }

    public YopRequest(String appKey, String secretKey, String serverRoot) {
        Validate.notBlank(appKey, "AppKey is blank.");
        Validate.notBlank(secretKey, "SecretKey is blank.");
        Validate.notBlank(secretKey, "ServerRoot is blank.");
        this.appSDKConfig = new AppSDKConfig();
        this.appSDKConfig.setAppKey(appKey);
        if (StringUtils.endsWith(serverRoot, "/")) {
            this.appSDKConfig.setServerRoot(StringUtils.substring(serverRoot, 0, -1));
        } else {
            this.appSDKConfig.setServerRoot(serverRoot);
        }
        AppSDKConfig defaultAppSDKConfig = AppSDKConfigSupport.getDefaultAppSDKConfig();
        this.appSDKConfig.setYopPublicKey(defaultAppSDKConfig == null ?
                ConfigUtils.getDefaultYopPublicKey() : defaultAppSDKConfig.getYopPublicKey());
        this.secretKey = secretKey;
        init();
    }

    private void init() {
        headers.put(Headers.USER_AGENT, YopConstants.USER_AGENT);
        paramMap.put(YopConstants.APP_KEY, this.appSDKConfig.getAppKey());
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

    public String getSecretKey() {
        return secretKey;
    }

    public AppSDKConfig getAppSDKConfig() {
        return appSDKConfig;
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
