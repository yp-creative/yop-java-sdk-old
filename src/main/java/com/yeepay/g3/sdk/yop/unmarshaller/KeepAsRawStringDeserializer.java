package com.yeepay.g3.sdk.yop.unmarshaller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

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

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (JsonToken.START_OBJECT != jp.getCurrentToken() && JsonToken.START_ARRAY != jp.getCurrentToken()) {
            throw new IllegalArgumentException("KeepAsRawStringDeserializer can't be used here,current token is not a start of json object or json array!");
        }

        Object sourceRef = jp.getCurrentLocation().getSourceRef();
        if (!(sourceRef instanceof String)) {
            throw new IllegalArgumentException("source ref of json is not an instance of String,can't use KeepAsRawStringDeserializer here!");
        }

        String rawJson = (String) sourceRef;
        int startLocation = (int) jp.getCurrentLocation().getCharOffset();
        jp.skipChildren();
        int endLocation = (int) jp.getCurrentLocation().getCharOffset();
        return rawJson.substring(startLocation - 1, endLocation);
    }
}
