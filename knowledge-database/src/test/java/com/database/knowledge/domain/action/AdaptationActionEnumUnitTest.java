package com.database.knowledge.domain.action;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.getAdaptationActionEnumByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.database.knowledge.exception.InvalidAdaptationActionException;

class AdaptationActionEnumUnitTest {

	private static Stream<Arguments> parametersGetEnumTestParams() {
		return Stream.of(
				arguments("Add server", ADD_SERVER),
				arguments("Increase job deadline priority", INCREASE_DEADLINE_PRIORITY),
				arguments("Increase job power priority", INCREASE_POWER_PRIORITY),
				arguments("Increase Green Source selection chance", INCREASE_GREEN_SOURCE_PERCENTAGE),
				arguments("Increase Green Source weather prediction error", INCREASE_GREEN_SOURCE_ERROR),
				arguments("Connecting Green Source", CONNECT_GREEN_SOURCE)
		);
	}

	@ParameterizedTest
	@MethodSource("parametersGetEnumTestParams")
	@DisplayName("Test get adaptation action by name")
	void testGetAdaptationActionByName(final String actionName, final AdaptationActionEnum result) {
		assertThat(getAdaptationActionEnumByName(actionName)).isEqualTo(result);
	}

	@Test
	@DisplayName("Test get adaptation action by name not found")
	void testGetAdaptationActionByNameNotFound() {
		assertThatThrownBy(() -> getAdaptationActionEnumByName("fake name"))
				.isInstanceOf(InvalidAdaptationActionException.class)
				.hasMessage("Adaptation action not found: Adaptation action with name fake name was not found");
	}
}
