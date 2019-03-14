package com.yeepay.g3.sdk.yop.client.router;

import com.yeepay.g3.sdk.yop.utils.CharacterConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * title: serverRoot组空间<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-13 10:42
 */
public class ServerRootSpace implements Serializable {

    private static final long serialVersionUID = -3149208992494791001L;

    private final String serverRoot;

    private final URL serverRootURL;

    private final String yosServerRoot;

    private final URL yosServerRootURL;

    private final String sandboxServerRoot;

    private final URL sandboxServerRootURL;


    public ServerRootSpace(String serverRoot, String yosServerRoot, String sandboxServerRoot) throws MalformedURLException {
        this.serverRoot = normalize(serverRoot);
        this.yosServerRoot = normalize(yosServerRoot);
        this.sandboxServerRoot = normalize(sandboxServerRoot);

        this.serverRootURL = new URL(this.serverRoot);
        this.yosServerRootURL = new URL(this.yosServerRoot);
        this.sandboxServerRootURL = new URL(this.sandboxServerRoot);
    }

    private String normalize(String serverRoot) {
        if (StringUtils.endsWith(serverRoot, CharacterConstants.SLASH)) {
            return StringUtils.substring(serverRoot, 0, serverRoot.length() - 1);
        }
        return serverRoot;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public URL getServerRootURL() {
        return serverRootURL;
    }

    public String getYosServerRoot() {
        return yosServerRoot;
    }

    public URL getYosServerRootURL() {
        return yosServerRootURL;
    }

    public String getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public URL getSandboxServerRootURL() {
        return sandboxServerRootURL;
    }
}
