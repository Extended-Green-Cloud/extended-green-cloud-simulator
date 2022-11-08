package com.greencloud.application.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Mapper used to parse the retrieved message objects
 */
public final class JsonMapper {

	private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	private JsonMapper() {
	}

	public static ObjectMapper getMapper() {
		return MAPPER;
	}
}
