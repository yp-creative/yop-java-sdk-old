package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.sdk.yop.YopServiceException;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import com.yeepay.g3.sdk.yop.utils.ValidateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * title: sdk配置（老版本）<br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class SDKConfigSupport {

    private static final Logger LOGGER = Logger.getLogger(SDKConfigSupport.class);

    private static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_FILE_SEPARATOR = ",";

    private static final String SDK_CONFIG_DIR = "config";

    private static final String DEFAULT_SDK_CONFIG_FILE_PATH = "config/yop_sdk_config_default.json";

    private static final String DEFAULT_SDK_CONFIG_KEY = "default";

    private static final Pattern SDK_CONFIG_FILE_NAME_PATTERN = Pattern.compile("^yop_sdk_config_(.+).json$");

    private static final ConcurrentMap<String, SDKConfig> CONFIGS = new ConcurrentHashMap<String, SDKConfig>();

    private static SDKConfig defaultSDKConfig;

    private static boolean customDefault;

    private static volatile boolean inited = false;

    public static SDKConfig getDefaultSDKConfig() {
        initSDKConfig();
        return defaultSDKConfig;
    }

    public static Map<String, SDKConfig> getSDKConfigs() {
        initSDKConfig();
        return Collections.unmodifiableMap(CONFIGS);
    }

    public static boolean isCustomDefault() {
        initSDKConfig();
        return customDefault;
    }

    private static void initSDKConfig() {
        if (inited) {
            return;
        }
        synchronized (SDKConfigSupport.class) {
            if (inited) {
                return;
            }
            String configFileProperty = System.getProperty(SDK_CONFIG_FILE_PROPERTY_KEY);
            if (StringUtils.isNotEmpty(configFileProperty)) {
                String[] configFiles = StringUtils.split(configFileProperty, SDK_CONFIG_FILE_SEPARATOR);
                for (String absolutePath : configFiles) {
                    String fileName = StringUtils.substringAfterLast(absolutePath, File.separator);
                    Matcher matcher = SDK_CONFIG_FILE_NAME_PATTERN.matcher(fileName);
                    if (matcher.matches()) {
                        SDKConfig sdkConfig = loadConfig(absolutePath);
                        ValidateUtils.checkCustomSDKConfig(sdkConfig);
                        String fileNameSuffix = matcher.group(1);
                        if (StringUtils.equals(fileNameSuffix, DEFAULT_SDK_CONFIG_KEY)) {
                            defaultSDKConfig = sdkConfig;
                        }
                        CONFIGS.put(StringUtils.replace(sdkConfig.getAppKey(), ":", ""), sdkConfig);
                    } else {
                        LOGGER.warn("Illegal SDkConfig File Name:" + fileName);
                    }
                }
            } else {
                List<String> fileNames = loadConfigFilesFromClassPath();
                if (CollectionUtils.isNotEmpty(fileNames)) {
                    for (String fileName : fileNames) {
                        Matcher matcher = SDK_CONFIG_FILE_NAME_PATTERN.matcher(fileName);
                        if (matcher.matches()) {
                            SDKConfig sdkConfig = loadConfig(SDK_CONFIG_DIR + File.separator + fileName);
                            ValidateUtils.checkCustomSDKConfig(sdkConfig);
                            String fileNameSuffix = matcher.group(1);
                            if (StringUtils.equals(fileNameSuffix, DEFAULT_SDK_CONFIG_KEY)) {
                                defaultSDKConfig = sdkConfig;
                            }
                            CONFIGS.put(StringUtils.replace(sdkConfig.getAppKey(), ":", ""), sdkConfig);
                        } else {
                            LOGGER.warn("Illegal SDkConfig File Name:" + fileName);
                        }
                    }
                }
            }
            if (defaultSDKConfig == null) {
                if (CONFIGS.size() == 1) {
                    defaultSDKConfig = CONFIGS.values().iterator().next();
                    customDefault = true;
                } else {
                    defaultSDKConfig = loadConfig(DEFAULT_SDK_CONFIG_FILE_PATH);
                    customDefault = false;
                }
            } else {
                customDefault = true;
            }
            inited = true;
        }
    }

    private static List<String> loadConfigFilesFromClassPath() {
        List<String> filenames = new ArrayList<String>();
        try {
            //无法读取jar包的目录
//            InputStream in = ConfigUtils.getContextClassLoader().getResourceAsStream(SDK_CONFIG_DIR);
            URL url = ConfigUtils.getContextClassLoader().getResource(SDK_CONFIG_DIR);
            if (url != null) {
                File file = new File(url.toURI());
                if (file.isDirectory()) {
                    InputStream in = url.openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String resource;
                    while ((resource = br.readLine()) != null) {
                        filenames.add(resource);
                    }
                } else {
                    LOGGER.warn("Sdk config directory is a file.");
                }
            } else {
                LOGGER.warn("SDK config directory does't exist.");
            }
        } catch (Exception ex) {
            LOGGER.debug("Unable to read sdk config dir.");
        }
        return filenames;
    }

    private static SDKConfig loadConfig(String configFile) {
        InputStream fis = null;
        SDKConfig config;
        try {
            fis = ConfigUtils.getInputStream(configFile);
            config = JsonUtils.loadFrom(fis, SDKConfig.class);
        } catch (Exception ex) {
            throw new YopServiceException(ex, "Errors occurred when loading SDK Config.");
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (StringUtils.endsWith(config.getServerRoot(), "/")) {
            config.setServerRoot(StringUtils.substring(config.getServerRoot(), 0, -1));
        }
        return config;
    }

}

