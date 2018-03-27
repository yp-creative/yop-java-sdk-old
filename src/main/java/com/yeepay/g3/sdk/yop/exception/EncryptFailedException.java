package com.yeepay.g3.sdk.yop.exception;

/**
 * title: 解密失败<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/22 下午2:36
 */
public class EncryptFailedException extends YopClientException {

    private static final long serialVersionUID = -5365630128856068164L;

    public EncryptFailedException(String message) {
        super(message);
    }

    public EncryptFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
