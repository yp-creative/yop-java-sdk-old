/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.encrypt;

/**
 * HEX转换工具类
 * @author：junning.li    
 * @since：2011-6-1 下午06:53:51 
 * @version:   
 */
public class Hex {
	
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
	
	public static byte[] fromHex(String input) {
		if (input == null)
			return null;
		byte output[] = new byte[input.length() / 2];
		for (int i = 0; i < output.length; i++)
			output[i] = (byte) Integer.parseInt(
					input.substring(i * 2, (i + 1) * 2), 16);
		return output;
	}
}
