package com.yeepay.g3.sdk.yop.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * title: <br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 17/4/24 下午3:15
 */
public class CertificateConfig implements Serializable{

    private static final long serialVersionUID = -6377916283927611130L;

    @JsonProperty("password")
    private String password;

    @JsonProperty("path")
    private String path;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
