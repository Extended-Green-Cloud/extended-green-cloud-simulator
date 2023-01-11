package com.greencloud.application.agents.greenenergy.management;

import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.adaptation.InitiateGreenSourceDeactivation;
import com.greencloud.application.agents.greenenergy.behaviour.adaptation.InitiateNewServerConnection;
import com.greencloud.commons.managingsystem.planner.AdjustGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.ImmutableAdjustGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.ImmutableChangeGreenSourceConnectionParameters;

import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
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
		doReturn(greenEnergyAdaptationManagement).when(mockGreenEnergyAgent).adapt();
		doNothing().when(manager).updateGreenSourceGUI();
	}

	@ParameterizedTest
	@MethodSource("parametersGetByIdAndStart")
	@DisplayName("Test adapt green energy agent weather prediction error")
	void testAdaptAgentWeatherPredictionError(AdjustGreenSourceErrorParameters params, double expectedResult) {
		mockGreenEnergyAgent.setWeatherPredictionError(INITIAL_WEATHER_PREDICTION_ERROR);
		greenEnergyAdaptationManagement.adaptAgentWeatherPredictionError(params);

		assertThat(mockGreenEnergyAgent.getWeatherPredictionError()).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Test connecting green source to server")
	void testConnectNewServerToGreenSource() {
		var adaptationParams = ImmutableChangeGreenSourceConnectionParameters.builder()
				.serverName("test_server")
				.build();
		var message = new ACLMessage(REQUEST);

		greenEnergyAdaptationManagement.connectNewServerToGreenSource(adaptationParams, message);
		verify(mockGreenEnergyAgent).addBehaviour(argThat(arg -> arg instanceof InitiateNewServerConnection));
	}

	@Test
	@DisplayName("Test disconnecting green source from server")
	void testDisconnectServerFromGreenSource() {
		var adaptationParams = ImmutableChangeGreenSourceConnectionParameters.builder()
				.serverName("test_server")
				.build();
		var message = new ACLMessage(REQUEST);

		greenEnergyAdaptationManagement.disconnectGreenSourceFromServer(adaptationParams, message);
		verify(mockGreenEnergyAgent).addBehaviour(argThat(arg -> arg instanceof InitiateGreenSourceDeactivation));

		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()).satisfies(state -> {
			assertThat(state.getOriginalAdaptationMessage()).isEqualTo(message);
			assertThat(state.isBeingDisconnected()).isTrue();
		});
	}

	private static Stream<Arguments> parametersGetByIdAndStart() {
		return Stream.of(
				arguments(
						ImmutableAdjustGreenSourceErrorParameters.builder()
								.percentageChange(0.02)
								.build(),
						0.04
				),
				arguments(
						ImmutableAdjustGreenSourceErrorParameters.builder()
								.percentageChange(0.08)
								.build(),
						0.1
				),
				arguments(
						ImmutableAdjustGreenSourceErrorParameters.builder()
								.percentageChange(-0.01)
								.build(),
						0.01
				)
		);
	}
}
