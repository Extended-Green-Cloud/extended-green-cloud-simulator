package com.greencloud.application.agents.greenenergy;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.commons.managingsystem.planner.ImmutableConnectGreenSourceParameters;
import com.greencloud.commons.managingsystem.planner.ImmutableAdjustGreenSourceErrorParameters;

import jade.lang.acl.ACLMessage;

class AbstractGreenEnergyAgentUnitTest {

	private static final double INITIAL_WEATHER_PREDICTION_ERROR = 0.02;

	@Mock
	private GreenEnergyAgent agent;
	@Mock
	private GreenEnergyAdaptationManagement mockAdaptationManagement;

	@BeforeEach
	void init() {
		agent = spy(GreenEnergyAgent.class);
		mockAdaptationManagement = spy(new GreenEnergyAdaptationManagement(agent));
		agent.adaptationManagement = mockAdaptationManagement;
		var manager = spy(new GreenEnergyStateManagement(agent));

		doReturn(manager).when(agent).manage();
		doNothing().when(manager).updateGreenSourceGUI();
	}

	@Test
	@DisplayName("Test executing adaptation action for incrementing error")
	void testExecuteActionIncrementError() {
		var adaptationAction = getAdaptationAction(AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR);
		var adaptationParams = ImmutableAdjustGreenSourceErrorParameters.builder()
				.percentageChange(0.04)
				.build();
		agent.setWeatherPredictionError(INITIAL_WEATHER_PREDICTION_ERROR);
		agent.executeAction(adaptationAction, adaptationParams);

		assertThat(agent.getWeatherPredictionError()).isEqualTo(0.06);
	}

	@Test
	@DisplayName("Test executing adaptation action for decrementing error")
	void testExecuteActionDecrementError() {
		var adaptationAction = getAdaptationAction(AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR);
		var adaptationParams = ImmutableAdjustGreenSourceErrorParameters.builder()
				.percentageChange(-0.04)
				.build();
		agent.setWeatherPredictionError(0.06);
		agent.executeAction(adaptationAction, adaptationParams);

		assertThat(agent.getWeatherPredictionError()).isCloseTo(0.02, Offset.offset(0.0001));
	}

	@Test
	@DisplayName("Test executing adaptation action for connecting green source to server")
	void testExecuteActionForConnectingGreenSource() {
		var adaptationAction = getAdaptationAction(AdaptationActionEnum.CONNECT_GREEN_SOURCE);
		var adaptationParams = ImmutableConnectGreenSourceParameters.builder()
				.serverName("test_server")
				.build();
		var message =  new ACLMessage(REQUEST);
		agent.executeAction(adaptationAction, adaptationParams, message);

		verify(mockAdaptationManagement).connectNewServerToGreenSource(adaptationParams, message);
	}
}
