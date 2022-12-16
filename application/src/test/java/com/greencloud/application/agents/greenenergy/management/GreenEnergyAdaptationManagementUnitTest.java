package com.greencloud.application.agents.greenenergy.management;

import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.adaptation.InitiateNewServerConnection;
import com.greencloud.commons.managingsystem.planner.ImmutableConnectGreenSourceParameters;
import com.greencloud.commons.managingsystem.planner.ImmutableIncrementGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;

import jade.lang.acl.ACLMessage;

class GreenEnergyAdaptationManagementUnitTest {

	private static final double INITIAL_WEATHER_PREDICTION_ERROR = 0.02;
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

	@Test
	@DisplayName("Test connecting green source to server")
	void testConnectNewServerToGreenSource() {
		var adaptationParams = ImmutableConnectGreenSourceParameters.builder()
				.serverName("test_server")
				.build();
		var message = new ACLMessage(REQUEST);

		greenEnergyAdaptationManagement.connectNewServerToGreenSource(adaptationParams, message);
		verify(mockGreenEnergyAgent).addBehaviour(argThat(arg -> arg instanceof InitiateNewServerConnection));
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
