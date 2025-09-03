package org.greencloud.commons.utils.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Class contains methods used to expand the default ObjectMapper with custom deserializers.
 */
public class MapperExpander {

	/**
	 * Method used to expand the default ObjectMapper with a custom Instant deserializer.
	 */
	public static ObjectMapper expandMapperWithInstantDeserializer(final ObjectMapper mapper) {
		final SimpleModule dateDeserializeModule = new SimpleModule();
		dateDeserializeModule.addKeyDeserializer(Instant.class, new KeyDeserializer() {
			@Override
			public Object deserializeKey(final String key, final DeserializationContext ctxt) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");
				return LocalDateTime.parse(key, formatter).toInstant(ZoneOffset.UTC);
			}
		});

		mapper.registerModule(dateDeserializeModule);
		return mapper;
	}
}
