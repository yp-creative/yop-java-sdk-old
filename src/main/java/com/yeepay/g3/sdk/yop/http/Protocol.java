package com.yeepay.g3.sdk.yop.http;

/**
 * title: 通讯协议<br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 16/2/18 17:00
 */
public enum Protocol {

    /**
     * 不会占用额外的服务器资源, 但不安全
     */
    HTTP("http", 80),

    /**
     * 安全, 但会占用额外的服务器资源
     */
    HTTPS("https", 443);

    private String protocol;
    private int defaultPort;

    Protocol(String protocol, int defaultPort) {
        this.protocol = protocol;
        this.defaultPort = defaultPort;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    @Override
    public String toString() {
        return this.protocol;
    }

}
