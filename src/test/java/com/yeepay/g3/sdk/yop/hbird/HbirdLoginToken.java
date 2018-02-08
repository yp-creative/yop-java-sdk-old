package com.yeepay.g3.sdk.yop.hbird;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/1/31 下午2:36
 */
public class HbirdLoginToken {

    private String status;

    @JsonProperty("oauth2AccessToken")
    private OAuth2AccessToken oAuth2AccessToken;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OAuth2AccessToken getoAuth2AccessToken() {
        return oAuth2AccessToken;
    }

    public void setoAuth2AccessToken(OAuth2AccessToken oAuth2AccessToken) {
        this.oAuth2AccessToken = oAuth2AccessToken;
    }

}
