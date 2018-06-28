package com.yeepay.g3.sdk.yop.utils;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 2018/6/28 下午1:36
 */
public class FileUtilsTest {

    @Test
    public void getFileName() {

    }

    @Test
    public void getXmlFileExt() {
        File file = new File("src/test/resources/log4j.xml");
        String fileExt = "unKnown";
        if (file.exists()) {
            fileExt = FileUtils.getFileExt(file);
        }
        assertEquals(".xml", fileExt);
    }

    @Test
    public void getPdfFileExt() {
        File file = new File("/Users/dreambt/周报的目标原则方法闭环.pdf");
        String fileExt = "unKnown";
        if (file.exists()) {
            fileExt = FileUtils.getFileExt(file);
        }
        assertEquals(".pdf", fileExt);
    }

    @Test
    public void getPngFileExt() {
        File file = new File("/Users/dreambt/SiteMesh Flow Diagram.png");
        String fileExt = "unKnown";
        if (file.exists()) {
            fileExt = FileUtils.getFileExt(file);
        }
        assertEquals(".png", fileExt);

        try {
            FileInputStream is = new FileInputStream(file);
            fileExt = FileUtils.getFileExt(is);
            assertEquals(".png", fileExt);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}