package com.yeepay.g3.sdk.yop.unmarshaller;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * <pre>
 *    将响应对象流化成JSON。 {@link ObjectMapper}是线程安全的。
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class JacksonJsonMarshaller implements YopMarshaller {

	private static ObjectMapper objectMapper;

	@Override
	public void marshal(Object object, OutputStream outputStream) {
		try {
			JsonGenerator jsonGenerator = getObjectMapper().getFactory()
					.createGenerator(outputStream, JsonEncoding.UTF8);
			getObjectMapper().writeValue(jsonGenerator, object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T unmarshal(String content, Class<T> objectType) {
		try {
			return getObjectMapper().readValue(content, objectType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ObjectMapper getObjectMapper() throws IOException {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss"));
			objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.setSerializationInclusion(Include.NON_EMPTY);
			objectMapper
					.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
		}
		return objectMapper;
	}
}
