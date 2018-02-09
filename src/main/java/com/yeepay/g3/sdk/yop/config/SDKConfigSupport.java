package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.sdk.yop.YopServiceException;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
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

    private static final String DEFAULT_SDK_CONFIG_DIR = "/config";

    private static final String DEFAULT_SDK_CONFIG_KEY = "default";

    private static final Pattern SDK_CONFIG_FILE_NAME_PATTERN = Pattern.compile("^yop_sdk_config_(.+).json$");

    private static final ConcurrentMap<String, SDKConfig> CONFIGS = new ConcurrentHashMap<String, SDKConfig>();

    private static volatile SDKConfig DEFAULT_SDK_CONFIG;

    private static volatile boolean inited = false;

    public static SDKConfig getConfig(String appKey) {
        if (StringUtils.isEmpty(appKey)) {
            throw new YopServiceException("AppKey must be specified.");
        }
        initSDKConfig();
        return CONFIGS.get(appKey);
    }

    public static SDKConfig getDefaultConfig() {
        initSDKConfig();
        return DEFAULT_SDK_CONFIG;
    }

    public static Map<String, SDKConfig> getSDKConfigs() {
        initSDKConfig();
        return Collections.unmodifiableMap(CONFIGS);
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
                        String appKey = matcher.group(1);
                        CONFIGS.put(appKey, loadConfig(absolutePath));
                    } else {
                        LOGGER.warn("Illegal SDkConfig File Name:" + fileName);
                    }
                }
            } else {
                List<String> fileNames = loadConfigFilesFromClassPath();
                for (String fileName : fileNames) {
                    Matcher matcher = SDK_CONFIG_FILE_NAME_PATTERN.matcher(fileName);
                    if (matcher.matches()) {
                        String appKey = matcher.group(1);
                        CONFIGS.put(appKey, loadConfig(DEFAULT_SDK_CONFIG_DIR + File.separator + fileName));
                    } else {
                        LOGGER.warn("Illegal SDkConfig File Name:" + fileName);
                    }
                }
            }
            if (CONFIGS.size() == 0) {
                throw new YopServiceException("No Available SDKConfig File can be found.");
            } else if (CONFIGS.size() == 1) {
                DEFAULT_SDK_CONFIG = CONFIGS.values().iterator().next();
            } else {
                DEFAULT_SDK_CONFIG = CONFIGS.get(DEFAULT_SDK_CONFIG_KEY);
            }
            inited = true;
        }
    }

    private static List<String> loadConfigFilesFromClassPath() {
        List<String> filenames = new ArrayList<String>();
        try {
            InputStream in = ConfigUtils.getResourceAsStream(DEFAULT_SDK_CONFIG_DIR);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        } catch (IOException ex) {
            throw new YopServiceException(ex, "IoException occurred when load config file from classPath:" + DEFAULT_SDK_CONFIG_DIR);
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

