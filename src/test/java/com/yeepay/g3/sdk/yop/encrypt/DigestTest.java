package com.yeepay.g3.sdk.yop.encrypt;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * title: <br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 16/2/19 15:10
 */
public class DigestTest {

    @Test
    public void testMd5Digest() throws Exception {
        byte[] data = "0123456789".getBytes(YopConstants.ENCODING);
        assertEquals(Hex.toHex(data), toHex(data));
    }

    public static String toHex(byte input[]) {
        if (input == null)
            return null;
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16)
                output.append("0");
            output.append(Integer.toString(current, 16));
        }
        return output.toString();
    }

    @Test
    public void testSha1Digest() throws Exception {

    }

    @Test
    public void testSha256Hex() throws Exception {

    }

    @Test
    public void testDigest() throws Exception {

    }

}