package com.greencloud.application.mapper;

import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonMapperUnitTest {

	@Test
	@DisplayName("Test get mapper")
	void testGetMapper() {
		var mapper = getMapper();

		assertThat(mapper.getRegisteredModuleIds())
				.hasSize(2)
				.containsExactlyInAnyOrder(
						"com.fasterxml.jackson.datatype.guava.GuavaModule",
						"jackson-datatype-jsr310"
				);
	}
}
