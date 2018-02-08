package com.yeepay.g3.sdk.yop.utils;

import org.apache.log4j.Logger;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:48
 */
public final class InternalConfig {

    private static final Logger LOGGER = Logger.getLogger(InternalConfig.class);

    private static final String SP_SDK_CONFIG_FILE = "yop.sdk.config.file";

    /**
     * 1.如果配置file://开头，则是系统绝对路径
     * 2.如果是/开头，则是classpath下相对路径
     */
    private static final String DEFAULT_SDK_CONFIG_FILE = "/config/yop_sdk_config_default.json";

    public static final String PROTOCOL_VERSION = "yop-auth-v2";

    private static final String CONFIG_FILE_ABSOLUTE_PATH_PREFIX = "file://";
    private static final String CONFIG_FILE_CLASS_PATH_PREFIX = "classpath:";
    private static final String SLASH = "/";
    public static final String SERVER_ROOT = "https://open.yeepay.com/yop-center";

    public static int CONNECT_TIMEOUT = 30000;
    public static int READ_TIMEOUT = 60000;

    public static int MAX_CONN_TOTAL = 200;
    public static int MAX_CONN_PER_ROUTE = 0;

    public static boolean TRUST_ALL_CERTS = false;
}
