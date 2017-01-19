package com.yeepay.g3.sdk.yop.enums;

/**
 * 支持的响应的格式类型
 */
public enum FormatType {

	xml, json;

	public static FormatType getFormat(String value) {
		if (value == null || value.trim().length() == 0) {
			return json;
		} else {
			try {
				return FormatType.valueOf(value.toLowerCase());
			} catch (IllegalArgumentException e) {
				return json;
			}
		}
	}
}
