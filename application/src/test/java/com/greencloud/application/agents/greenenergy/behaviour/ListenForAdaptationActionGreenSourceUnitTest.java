package com.greencloud.application.agents.greenenergy.behaviour;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_REQUEST;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.application.behaviours.ListenForAdaptationAction;
import com.greencloud.commons.managingsystem.planner.AdjustGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.ConnectGreenSourceParameters;
import com.greencloud.commons.managingsystem.planner.ImmutableAdjustGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.ImmutableConnectGreenSourceParameters;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ListenForAdaptationActionGreenSourceUnitTest {

	private static final double INITIAL_WEATHER_PREDICTION_ERROR = 0.02;
	@Mock
	private GreenEnergyAgent greenEnergyAgent;
	@Mock
	private GreenEnergyAdaptationManagement greenEnergyAdaptationManagement;

	private ListenForAdaptationAction listenForAdaptationAction;

	@BeforeEach
	void init() {
		greenEnergyAgent = spy(GreenEnergyAgent.class);
		greenEnergyAdaptationManagement = spy(new GreenEnergyAdaptationManagement(greenEnergyAgent));

		greenEnergyAgent.setAdaptationManagement(greenEnergyAdaptationManagement);
		greenEnergyAgent.setWeatherPredictionError(INITIAL_WEATHER_PREDICTION_ERROR);
		var manager = spy(new GreenEnergyStateManagement(greenEnergyAgent));

		doReturn(manager).when(greenEnergyAgent).manage();
		doNothing().when(manager).updateGreenSourceGUI();

		listenForAdaptationAction = new ListenForAdaptationAction(greenEnergyAgent);
	}

	@Test
	@DisplayName("Test receiving adaptation message for incrementing prediction error")
	void testIncrementErrorAction() {
		var testMessage = prepareTestAdjustErrorMessage(INCREASE_GREEN_SOURCE_ERROR, 0.05);
		when(greenEnergyAgent.receive(EXECUTE_ACTION_REQUEST)).thenReturn(testMessage);

		listenForAdaptationAction.action();

		verify(greenEnergyAgent).executeAction(
				argThat((data -> data.getAction().equals(INCREASE_GREEN_SOURCE_ERROR))),
				argThat((data) -> data instanceof AdjustGreenSourceErrorParameters &&
						((AdjustGreenSourceErrorParameters) data).getPercentageChange() == 0.05));

		verify(greenEnergyAgent).send(any());

		assertThat(greenEnergyAgent.getWeatherPredictionError()).isEqualTo(0.07);
	}

	@Test
	@DisplayName("Test receiving adaptation message for decrementing prediction error")
	void testDecrementErrorAction() {
		var testMessage = prepareTestAdjustErrorMessage(DECREASE_GREEN_SOURCE_ERROR, -0.01);
		when(greenEnergyAgent.receive(EXECUTE_ACTION_REQUEST)).thenReturn(testMessage);

		listenForAdaptationAction.action();

		verify(greenEnergyAgent).executeAction(
				argThat((data -> data.getAction().equals(DECREASE_GREEN_SOURCE_ERROR))),
				argThat((data) -> data instanceof AdjustGreenSourceErrorParameters &&
						((AdjustGreenSourceErrorParameters) data).getPercentageChange() == -0.01));

		verify(greenEnergyAgent).send(any());

		assertThat(greenEnergyAgent.getWeatherPredictionError()).isEqualTo(0.01);
	}

	@Test
	@DisplayName("Test receiving adaptation message for connecting green source with server")
	void testConnectGreenSourceAction() {
		var testMessage = prepareTestConnectGreenSourceMessage();
		when(greenEnergyAgent.receive(EXECUTE_ACTION_REQUEST)).thenReturn(testMessage);

		listenForAdaptationAction.action();

		verify(greenEnergyAgent).executeAction(
				argThat((data -> data.getAction().equals(CONNECT_GREEN_SOURCE))),
				argThat((data) -> data instanceof ConnectGreenSourceParameters &&
						Objects.equals(((ConnectGreenSourceParameters) data).getServerName(), "test_server")),
				eq(testMessage));

		verify(greenEnergyAdaptationManagement).connectNewServerToGreenSource(any(), eq(testMessage));
	}

	private ACLMessage prepareTestAdjustErrorMessage(AdaptationActionEnum action, double value) {
		return MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(action.toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(ImmutableAdjustGreenSourceErrorParameters.builder()
						.percentageChange(value)
						.build())
				.withReceivers(mock(AID.class))
				.build();
	}

	private ACLMessage prepareTestConnectGreenSourceMessage() {
		return MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(CONNECT_GREEN_SOURCE.toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(ImmutableConnectGreenSourceParameters.builder()
						.serverName("test_server")
						.build())
				.withReceivers(mock(AID.class))
				.build();
	}
}
