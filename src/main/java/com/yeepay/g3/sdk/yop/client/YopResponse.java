package com.yeepay.g3.sdk.yop.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.yeepay.g3.sdk.yop.enums.FormatType;
import com.yeepay.g3.sdk.yop.error.YopError;
import com.yeepay.g3.sdk.yop.unmarshaller.YopMarshallerUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author wang.bao
 * @version 1.0
 */
@JacksonXmlRootElement(localName = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class YopResponse {
    /**
     * 状态(SUCCESS/FAILURE)
     */
    @XmlElement
    private String state;

    /**
     * 业务结果，非简单类型解析后为LinkedHashMap
     */
    @XmlElement
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
    private String stringResult;

    /**
     * 响应格式，冗余字段，跟Request的format相同，用于解析结果
     */
    private FormatType format;

    /**
     * 业务结果签名是否合法，冗余字段
     */
    private boolean validSign;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getStringResult() {
        return stringResult;
    }

    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    public boolean isValidSign() {
        return validSign;
    }

    /**
     * 响应结果签名是否合法（响应结果数据防篡改）
     */
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
            return YopMarshallerUtils.unmarshal(stringResult, format,
                    objectType);
        }
        return null;
    }

    /**
     * 将业务结果转换为自定义对象（参数映射）
     */
    public JsonNode parse() {
        if (StringUtils.isNotBlank(stringResult) && format == FormatType.json) {
            return YopMarshallerUtils.parse(stringResult);
        }
        return null;
    }

    public String toString() {
        String[] excludeFieldNames = new String[]{"stringResult"};
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .setExcludeFieldNames(excludeFieldNames)
                .toString();
    }
}
