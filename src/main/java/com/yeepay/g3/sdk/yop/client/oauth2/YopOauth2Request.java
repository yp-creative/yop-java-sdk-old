package com.yeepay.g3.sdk.yop.client.oauth2;

import com.yeepay.g3.sdk.yop.client.YopRequest;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/2/20 下午1:57
 */
public class YopOauth2Request extends YopRequest {

    public YopOauth2Request() {
    }

    public YopOauth2Request(String appKey, String secretKey) {
        super(appKey, secretKey);
    }

    public YopOauth2Request(String appKey, String secretKey, boolean androidMode) {
        super(appKey, secretKey, androidMode);
    }

    public YopOauth2Request(String appKey, String secretKey, String serverRoot) {
        super(appKey, secretKey, serverRoot);
    }

    public YopOauth2Request(String appKey, String secretKey, String serverRoot, boolean androidMode) {
        super(appKey, secretKey, serverRoot, androidMode);
    }

}
