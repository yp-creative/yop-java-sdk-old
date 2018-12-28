package com.yeepay.g3.sdk.yop.client;

import java.io.IOException;

/**
 * <pre>
 * 非对称 Client，已废弃，请使用 YopRsaClient
 * </pre>
 */
@Deprecated
public class YopClient3 extends YopRsaClient {

    @Deprecated
    public static YopResponse postRsa(String apiUri, YopRequest request) throws IOException {
        return post(apiUri, request);
    }

    @Deprecated
    public static YopResponse uploadRsa(String apiUri, YopRequest request) throws IOException {
        return upload(apiUri, request);
    }

}
