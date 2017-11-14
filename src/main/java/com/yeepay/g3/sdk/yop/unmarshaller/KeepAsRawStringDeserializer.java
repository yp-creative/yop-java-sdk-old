package com.yeepay.g3.sdk.yop.unmarshaller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;

/**
 * title: 保留Json对象为raw string的形式，不尝试解析它<br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 17/10/19 下午8:36
 */
public class KeepAsRawStringDeserializer extends JsonDeserializer<String> {

    protected static final Logger LOGGER = Logger.getLogger(KeepAsRawStringDeserializer.class);

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (JsonToken.START_OBJECT != jp.getCurrentToken()) {
            throw new IllegalArgumentException("KeepAsRawStringDeserializer can't be used here,current token is not a start of json object!");
        }

        int startLocation = (int) jp.getCurrentLocation().getCharOffset();
        jp.skipChildren();
        int endLocation = (int) jp.getCurrentLocation().getCharOffset();

        StringReader sr = (StringReader) jp.getInputSource();
        return getStringViaReflection(sr).substring(startLocation, endLocation + 1);
    }

    /**
     * StringReader -> String
     * 没有通过StringReader的接口来获取String，是因为读取StringReader会改变对象本身的状态，可能会造成其它影响
     *
     * @return
     */
    private String getStringViaReflection(StringReader reader) {
        try {
            Field strField = StringReader.class.getDeclaredField("str");
            strField.setAccessible(true);
            return (String) strField.get(reader);
        } catch (Exception e) {
            LOGGER.error("error when get str from StringReader via reflection", e);
            return null;
        }
    }
}
