package com.yeepay.g3.sdk.yop.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.error.YopError;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.unmarshaller.KeepAsRawStringDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.Map;

/**
 * @author wang.bao
 * @version 1.0
 */
@JacksonXmlRootElement(localName = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class YopResponse {

    private transient Map<String, String> headers;

    /**
     * 状态(SUCCESS/FAILURE)
     */
    @XmlElement
    private String state;

    /**
     * 唯一请求标示，需要技术支持时提供该字符串将有助于问题的快速解决
     */
    private String requestId;

    /**
     * 业务结果，非简单类型解析后为LinkedHashMap
     */
    @JsonIgnore
    private Object result;

    /**
     * 时间戳
     */
    @XmlElement
    private Long ts;

    /**
     * 结果签名，签名算法为Request指定算法，示例：SHA(<secret>stringResult<secret>)
     */
    @XmlElement
    private String sign;

    /**
     * 错误信息
     */
    @XmlElement
    private YopError error;

    /**
     * 字符串形式的业务结果，客户可自定义java类，使用YopMarshallerUtils.unmarshal做参数绑定
     */
    @JsonProperty("result")
    @JsonDeserialize(using = KeepAsRawStringDeserializer.class)
    private String stringResult;

    /**
     * 业务结果签名是否合法，冗余字段
     */
    @Deprecated
    private boolean validSign;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public YopError getError() {
        return error;
    }

    public void setError(YopError error) {
        this.error = error;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public YopResponse addHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = Maps.newHashMap();
        }
        this.headers.put(name, value);
        return this;
    }

    public String getStringResult() {
        return stringResult;
    }

    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    @Deprecated
    public boolean isValidSign() {
        return validSign;
    }

    /**
     * 响应结果签名是否合法（响应结果数据防篡改）
     */
    @Deprecated
    public void setValidSign(boolean validSign) {
        this.validSign = validSign;
    }

    /**
     * 业务是否成功
     */
    public boolean isSuccess() {
        return YopConstants.SUCCESS.equalsIgnoreCase(state);
    }

    /**
     * 将业务结果转换为自定义对象（参数映射）
     */
    public <T> T unmarshal(Class<T> objectType) {
        if (objectType != null && StringUtils.isNotBlank(stringResult)) {
            return JacksonJsonMarshaller.unmarshal(stringResult, objectType);
        }
        return null;
    }

    public InputStream getFile() {
        return (InputStream) result;
    }

    @Override
    public String toString() {
        String[] excludeFieldNames = new String[]{"headers,stringResult"};
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .setExcludeFieldNames(excludeFieldNames)
                .toString();
    }
}
