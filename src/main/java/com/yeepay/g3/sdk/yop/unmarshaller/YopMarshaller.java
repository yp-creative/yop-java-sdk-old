package com.yeepay.g3.sdk.yop.unmarshaller;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <pre>
 *   负责将请求方法返回的响应对应流化为相应格式的内容。<br>
 *   以及将字符串反序列化为相应的对象
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public interface YopMarshaller {
	/**
	 * 负责将请求方法返回的响应对应流化为相应格式的内容
	 */
	void marshal(Object object, OutputStream outputStream);

	/**
	 * 将字符串反序列化为相应的对象
	 */
	<T> T unmarshal(String content, Class<T> objectType);

	/**
	 * 返回ObjectMapper
	 */
	ObjectMapper getObjectMapper() throws IOException;
}
