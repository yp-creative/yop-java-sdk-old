package com.yeepay.g3.sdk.yop.hbird;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/1/31 下午2:37
 */
public class OAuth2AccessToken {

    private String value;

    private OAuth2RefreshToken refreshToken;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OAuth2RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(OAuth2RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

}
