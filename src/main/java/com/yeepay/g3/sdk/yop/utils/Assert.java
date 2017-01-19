package com.yeepay.g3.sdk.yop.utils;

import org.springframework.util.StringUtils;

/**
 * title: <br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 15/10/15 18:05
 */
public class Assert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

}
