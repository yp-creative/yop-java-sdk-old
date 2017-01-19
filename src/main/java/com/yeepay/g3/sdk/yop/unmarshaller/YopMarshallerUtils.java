package com.yeepay.g3.sdk.yop.unmarshaller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.yeepay.g3.sdk.yop.enums.FormatType;

/**
 * <pre>
 *   负责将请求方法返回的响应对应流化为相应格式的内容。<br>
 *   以及将字符串反序列化为相应的对象
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopMarshallerUtils {
	private static final Logger logger = Logger
			.getLogger(YopMarshallerUtils.class);

	private static Map<FormatType, YopMarshaller> marshallers = new HashMap<FormatType, YopMarshaller>();

	static {
		marshallers.put(FormatType.json, new JacksonJsonMarshaller());
		marshallers.put(FormatType.xml, new JacksonXmlMarshaller());
	}

	/**
	 * 负责将请求方法返回的响应对应流化为相应格式的内容
	 */
	public static void marshal(FormatType format, Object object,
			OutputStream outputStream) {
		YopMarshaller marshaller = marshallers.get(format);
		if (marshaller == null) {
			marshaller = marshallers.get(FormatType.json);
		}
		marshaller.marshal(object, outputStream);
	}

	/**
	 * 将字符串反序列化为相应的对象<br>
	 * xml格式的数据也将处理为json
	 */
	public static <T> T unmarshal(String content, FormatType format,
			Class<T> objectType) {
		YopMarshaller marshaller = marshallers.get(format);
		if (marshaller == null) {
			marshaller = marshallers.get(FormatType.json);
		}
		return marshaller.unmarshal(content, objectType);
	}

	/**
	 * 将字符串反序列化为Json对象<br>
	 * xml格式的数据也将处理为json<br>
	 * 通过JsonNode.path("path")获取子节点，通过asInt()等获取节点数据
	 *
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public static JsonNode parse(String content) {
		try {
			YopMarshaller marshaller = marshallers.get(FormatType.json);
			return marshaller.getObjectMapper().readTree(content);
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}
}
