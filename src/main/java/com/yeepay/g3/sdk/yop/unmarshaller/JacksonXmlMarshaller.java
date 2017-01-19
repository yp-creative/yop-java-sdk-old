package com.yeepay.g3.sdk.yop.unmarshaller;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;

/**
 * <pre>
 *    将响应对象流化成XML。 {@link ObjectMapper}是线程安全的。
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class JacksonXmlMarshaller implements YopMarshaller {

	private static XmlMapper objectMapper;

	@Override
	public void marshal(Object object, OutputStream outputStream) {
		try {
			getObjectMapper().writeValue(outputStream, object);
		} catch (IOException e) {
			e.printStackTrace();
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

	public XmlMapper getObjectMapper() throws IOException {
		if (objectMapper == null) {
			objectMapper = new XmlMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss"));
			objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.setSerializationInclusion(Include.NON_EMPTY);
			objectMapper
					.setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector(
							objectMapper.getTypeFactory()));
		}
		return objectMapper;
	}
}
