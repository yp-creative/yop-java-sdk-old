package com.yeepay.g3.sdk.yop.utils.checksum;

import com.google.common.primitives.UnsignedLong;
import com.yeepay.g3.sdk.yop.client.YopConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CheckedInputStream;

/**
 * title: CRC64工具类<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/5/31 15:30
 */
public class CRC64Utils {

    private static final Logger LOGGER = Logger.getLogger(CRC64Utils.class);


    public static String calculateMultiPartFileCrc64ecma(Map<String, Object> multiPartFiles) throws IOException {
        Map<String, Object> sortedFiles = new TreeMap<String, Object>(multiPartFiles);
        List<String> crc64ecmas = new ArrayList<String>(multiPartFiles.size());
        for (Map.Entry<String, Object> entry : sortedFiles.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof File) {
                crc64ecmas.add(getCRCValue((File) value));
            } else if (entry.getValue() instanceof InputStream) {
                crc64ecmas.add(getCRCValue((InputStream) value));
            } else if (value instanceof String) {
                crc64ecmas.add(getCRCValue((String) value));
            } else {
                LOGGER.warn("UnSupported type:" + value.getClass().getName() + " for file upload, ignore.");
            }
        }
        return StringUtils.join(crc64ecmas, "/");
    }

    public static String getCRCValue(File file) throws IOException {
        return getCRCValue(new FileInputStream(file));
    }

    public static String getCRCValue(String str) throws IOException {
        return getCRCValue(new ByteArrayInputStream(str.getBytes(YopConstants.ENCODING)));
    }

    public static String getCRCValue(InputStream in) throws IOException {
        CheckedInputStream checkedInputStream = new CheckedInputStream(in, new CRC64());
        while (checkedInputStream.read() != -1) {

        }
        return UnsignedLong.fromLongBits(checkedInputStream.getChecksum().getValue()).toString();
    }
}
