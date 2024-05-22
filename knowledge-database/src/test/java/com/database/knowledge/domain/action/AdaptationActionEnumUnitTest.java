package com.database.knowledge.domain.action;

import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.ADD_SERVER;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.CONNECT_GREEN_SOURCE;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.INCREASE_GREEN_SOURCE_ERROR;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.getAdaptationActionEnumByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.greencloud.commons.exception.InvalidAdaptationActionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;

class AdaptationActionEnumUnitTest {

	private static Stream<Arguments> parametersGetEnumTestParams() {
		return Stream.of(
				arguments("Add server", ADD_SERVER),
				arguments("Change Green Source selection weight", CHANGE_GREEN_SOURCE_WEIGHT),
				arguments("Increase Green Source weather prediction error", INCREASE_GREEN_SOURCE_ERROR),
				arguments("Connecting Green Source", CONNECT_GREEN_SOURCE)
		);
	}

	@ParameterizedTest
	@MethodSource("parametersGetEnumTestParams")
	@DisplayName("Test get adaptation action by name")
	void testGetAdaptationActionByName(final String actionName, final AdaptationActionTypeEnum result) {
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
