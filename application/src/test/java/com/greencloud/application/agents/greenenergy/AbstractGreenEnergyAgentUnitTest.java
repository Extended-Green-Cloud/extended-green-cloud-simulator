package com.greencloud.application.agents.greenenergy;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.INITIAL_WEATHER_PREDICTION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.commons.managingsystem.planner.ImmutableIncrementGreenSourceErrorParameters;

class AbstractGreenEnergyAgentUnitTest {

	private GreenEnergyAgent agent;

	@BeforeEach
	void init() {
		agent = spy(GreenEnergyAgent.class);
		agent.adaptationManagement = new GreenEnergyAdaptationManagement(agent);
		var manager = spy(new GreenEnergyStateManagement(agent));

		doReturn(manager).when(agent).manage();
		doNothing().when(manager).updateGreenSourceGUI();
	}

	@Test
	@DisplayName("Test executing adaptation action for incrementing error")
	void testExecuteActionIncrementError() {
		var adaptationAction = getAdaptationAction(AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR);
		var adaptationParams = ImmutableIncrementGreenSourceErrorParameters.builder()
				.percentageChange(0.04)
				.build();
		agent.setWeatherPredictionError(INITIAL_WEATHER_PREDICTION_ERROR);
		agent.executeAction(adaptationAction, adaptationParams);

		assertThat(agent.getWeatherPredictionError()).isEqualTo(0.06);
	}
}
