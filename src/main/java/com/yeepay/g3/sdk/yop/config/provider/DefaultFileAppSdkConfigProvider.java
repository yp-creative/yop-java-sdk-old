package com.yeepay.g3.sdk.yop.config.provider;

import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.config.support.ConfigUtils;
import com.yeepay.g3.sdk.yop.config.support.SDKConfigUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * title: 文件sdk配置provider<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 12:35
 */
public final class DefaultFileAppSdkConfigProvider extends BaseFixedAppSdkConfigProvider {

    private static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_FILE_SEPARATOR = ",";

    private static final String SDK_CONFIG_DIR = "config";

    private static final Pattern SDK_CONFIG_FILE_NAME_PATTERN = Pattern.compile("^yop_sdk_config_(.+).json$");

    @Override
    protected List<SDKConfig> loadCustomSdkConfig() {
        List<SDKConfig> customSdkConfigs = new ArrayList<SDKConfig>();
        String configFileProperty = System.getProperty(SDK_CONFIG_FILE_PROPERTY_KEY);
        if (StringUtils.isNotEmpty(configFileProperty)) {
            String[] configFiles = StringUtils.split(configFileProperty, SDK_CONFIG_FILE_SEPARATOR);
            for (String absolutePath : configFiles) {
                String fileName = StringUtils.substringAfterLast(absolutePath, File.separator);
                Matcher matcher = SDK_CONFIG_FILE_NAME_PATTERN.matcher(fileName);
                if (matcher.matches()) {
                    customSdkConfigs.add(SDKConfigUtils.loadConfig(absolutePath));
                } else {
                    logger.warn("Illegal SDkConfig File Name:" + fileName);
                }
            }
        } else {
            List<String> fileNames = loadConfigFilesFromClassPath();
            if (CollectionUtils.isNotEmpty(fileNames)) {
                for (String fileName : fileNames) {
                    Matcher matcher = SDK_CONFIG_FILE_NAME_PATTERN.matcher(fileName);
                    if (matcher.matches()) {
                        customSdkConfigs.add(SDKConfigUtils.loadConfig(SDK_CONFIG_DIR + File.separator + fileName));
                    } else {
                        logger.warn("Illegal SDkConfig File Name:" + fileName);
                    }
                }
            }
        }
        return customSdkConfigs;
    }

    private List<String> loadConfigFilesFromClassPath() {
        List<String> filenames = new ArrayList<String>();
        try {
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
                    logger.warn("Sdk config directory is a file.");
                }
            } else {
                logger.warn("SDK config directory does't exist.");
            }
        } catch (Exception ex) {
            logger.debug("Unable to read sdk config dir.");
        }
        return filenames;
    }

}
