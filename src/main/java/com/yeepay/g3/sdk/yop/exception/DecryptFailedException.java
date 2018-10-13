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
public class DecryptFailedException extends YopClientException {

    private static final long serialVersionUID = -1L;

    public DecryptFailedException(String message) {
        super(message);
    }

    public DecryptFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
