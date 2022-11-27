package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.INITIAL_WEATHER_PREDICTION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.managingsystem.planner.ImmutableIncrementGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;

class GreenEnergyAdaptationManagementUnitTest {

	@Mock
	private GreenEnergyAgent mockGreenEnergyAgent;

	private GreenEnergyAdaptationManagement greenEnergyAdaptationManagement;

	@BeforeEach
	void init() {
		mockGreenEnergyAgent = spy(GreenEnergyAgent.class);
		greenEnergyAdaptationManagement = new GreenEnergyAdaptationManagement(mockGreenEnergyAgent);
		var manager = spy(new GreenEnergyStateManagement(mockGreenEnergyAgent));

		doReturn(manager).when(mockGreenEnergyAgent).manage();
		doNothing().when(manager).updateGreenSourceGUI();
	}

	@ParameterizedTest
	@MethodSource("parametersGetByIdAndStart")
	@DisplayName("Test adapt green energy agent weather prediction error")
	void testAdaptAgentWeatherPredictionError(IncrementGreenSourceErrorParameters params, double expectedResult) {
		mockGreenEnergyAgent.setWeatherPredictionError(INITIAL_WEATHER_PREDICTION_ERROR);
		greenEnergyAdaptationManagement.adaptAgentWeatherPredictionError(params);

		assertThat(mockGreenEnergyAgent.getWeatherPredictionError()).isEqualTo(expectedResult);
	}

	private static Stream<Arguments> parametersGetByIdAndStart() {
		return Stream.of(
				arguments(
						ImmutableIncrementGreenSourceErrorParameters.builder()
								.percentageChange(0.02)
								.build(),
						0.04
				),
				arguments(
						ImmutableIncrementGreenSourceErrorParameters.builder()
								.percentageChange(0.08)
								.build(),
						0.1
				)
		);
	}
}
