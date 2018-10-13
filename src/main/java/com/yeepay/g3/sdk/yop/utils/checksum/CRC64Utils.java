package com.yeepay.g3.sdk.yop.utils.checksum;

import com.google.common.primitives.UnsignedLong;
import com.yeepay.g3.sdk.yop.client.YopConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(CRC64Utils.class);

    public static String calculateMultiPartFileCrc64ecma(Map<String, Object> multiPartFiles) throws IOException {
        Map<String, Object> sortedFiles = new TreeMap<String, Object>(multiPartFiles);
        List<String> crc64ecmas = new ArrayList<String>(multiPartFiles.size());
        for (Map.Entry<String, Object> entry : sortedFiles.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof File) {
                crc64ecmas.add(getCRCValue((File) value));
            } else if (value instanceof FileInputStream) {
                crc64ecmas.add(getCRCValue((FileInputStream) value));
            } else if (value instanceof InputStream) {
                InputStream in = (InputStream) value;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                crc64ecmas.add(getCRCValue(new ByteArrayInputStream(baos.toByteArray())));

                multiPartFiles.put(entry.getKey(), new ByteArrayInputStream(baos.toByteArray()));
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

    public static String getCRCValue(FileInputStream in) throws IOException {
        CheckedInputStream checkedInputStream = new CheckedInputStream(in, new CRC64());
        int skip = 0;
        while (checkedInputStream.read() != -1) {
            skip--;
        }

        in.skip(skip);
        return UnsignedLong.fromLongBits(checkedInputStream.getChecksum().getValue()).toString();
    }

    public static String getCRCValue(InputStream in) throws IOException {
        CheckedInputStream checkedInputStream = new CheckedInputStream(in, new CRC64());
        return UnsignedLong.fromLongBits(checkedInputStream.getChecksum().getValue()).toString();
    }
}
