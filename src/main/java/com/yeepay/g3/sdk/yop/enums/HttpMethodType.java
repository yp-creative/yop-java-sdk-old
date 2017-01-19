package com.yeepay.g3.sdk.yop.enums;

/**
 * <pre>
 * 请求类型的方法
 * </pre>
 * 
 * @author wang.bao
 * @version 1.0
 */
public enum HttpMethodType {

	GET, POST;

	public static HttpMethodType fromValue(String value) {
		if (GET.name().equalsIgnoreCase(value)) {
			return GET;
		} else if (POST.name().equalsIgnoreCase(value)) {
			return POST;
		} else {
			return POST;
		}
	}
}
