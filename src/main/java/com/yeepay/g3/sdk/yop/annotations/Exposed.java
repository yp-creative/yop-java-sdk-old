package com.yeepay.g3.sdk.yop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * title: 被注解的对象已经被暴漏出去了，不要随便修改或删除<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 17/4/24 上午12:29
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface Exposed {
    String[] exposedTo();
}
