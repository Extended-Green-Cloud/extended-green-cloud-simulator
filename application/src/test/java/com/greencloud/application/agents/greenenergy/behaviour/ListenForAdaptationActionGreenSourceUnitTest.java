package com.greencloud.application.agents.greenenergy.behaviour;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_REQUEST;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.application.behaviours.ListenForAdaptationAction;
import com.greencloud.commons.managingsystem.planner.ImmutableIncrementGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ListenForAdaptationActionGreenSourceUnitTest {

	private static final double INITIAL_WEATHER_PREDICTION_ERROR = 0.02;
	@Mock
	private GreenEnergyAgent greenEnergyAgent;

	private ListenForAdaptationAction listenForAdaptationAction;

	@BeforeEach
	void init() {
		greenEnergyAgent = spy(GreenEnergyAgent.class);
		greenEnergyAgent.setAdaptationManagement(new GreenEnergyAdaptationManagement(greenEnergyAgent));
		greenEnergyAgent.setWeatherPredictionError(INITIAL_WEATHER_PREDICTION_ERROR);
		var manager = spy(new GreenEnergyStateManagement(greenEnergyAgent));

		doReturn(manager).when(greenEnergyAgent).manage();
		doNothing().when(manager).updateGreenSourceGUI();

		listenForAdaptationAction = new ListenForAdaptationAction(greenEnergyAgent);
	}

	@Test
	@DisplayName("Test receiving adaptation message for incrementing prediction error")
	void testAction() {
		var testMessage = prepareTestMessage();
		when(greenEnergyAgent.receive(EXECUTE_ACTION_REQUEST)).thenReturn(testMessage);

		listenForAdaptationAction.action();

		verify(greenEnergyAgent).executeAction(
				argThat((data -> data.getAction().equals(INCREASE_GREEN_SOURCE_ERROR))),
				argThat((data) -> data instanceof IncrementGreenSourceErrorParameters &&
						((IncrementGreenSourceErrorParameters) data).getPercentageChange() == 0.05));

		verify(greenEnergyAgent).send(any());

		assertThat(greenEnergyAgent.getWeatherPredictionError()).isEqualTo(0.07);
	}

	private ACLMessage prepareTestMessage() {
		return MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(INCREASE_GREEN_SOURCE_ERROR.toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(ImmutableIncrementGreenSourceErrorParameters.builder()
						.percentageChange(0.05)
						.build())
				.withReceivers(mock(AID.class))
				.build();
	}
}
