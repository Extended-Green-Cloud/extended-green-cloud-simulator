package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static jade.core.AID.ISGUID;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult.createForAgentAction;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitiateAdaptationActionRequestTest {

	private static final Map<GoalEnum, Double> GOAL_QUALITIES = Map.of(
			MINIMIZE_USED_BACKUP_POWER, 0.5,
			MAXIMIZE_JOB_SUCCESS_RATIO, 0.3,
			DISTRIBUTE_TRAFFIC_EVENLY, 0.6);
	private static final AdaptationActionEnum ADAPTATION_ACTION_TYPE = AdaptationActionEnum.ADD_SERVER;
	private static final AID TEST_AID = new AID("test", ISGUID);
	private static final long DURATION = 7;

	@Mock
	ManagingAgent managingAgent;
	@Mock
	ManagingAgentNode managingAgentNode;
	@Mock
	TimescaleDatabase timescaleDatabase;
	@Mock
	Runnable mockRunnable;

	ACLMessage message;
	InitiateAdaptationActionRequest behaviour;

	@Captor
	ArgumentCaptor<VerifyAdaptationActionResult> captor;

	@BeforeEach
	void init() {
		mockRunnable = mock(Runnable.class);
		message = new ACLMessage(REQUEST);
		message.addReceiver(TEST_AID);
		message.setConversationId(ADAPTATION_ACTION_TYPE.toString());

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(timescaleDatabase);
		doNothing().when(managingAgentNode).logNewAdaptation(any(), any(), any());

		behaviour = new InitiateAdaptationActionRequest(managingAgent, message, GOAL_QUALITIES, mockRunnable,
				mockRunnable, getAdaptationAction(ADAPTATION_ACTION_TYPE).get(0));

	}

	@Test
	void shouldCorrectlyHandleInform() {
		// given
		var expectedVerifyBehaviour = createForAgentAction(managingAgent, now(), DURATION,
				getAdaptationAction(ADAPTATION_ACTION_TYPE).get(0),
				TEST_AID, GOAL_QUALITIES, mockRunnable);

		// when
		behaviour.handleInform(message);

		// then
		verify(managingAgent).addBehaviour(captor.capture());
		verify(mockRunnable).run();
		assertThat(captor.getValue())
				.as("Created behaviour should be equal to the expected one")
				.usingRecursiveComparison()
				.ignoringFields("actionTimestamp")
				.ignoringFields("executionTime")
				.ignoringFields("executionDuration")
				.isEqualTo(expectedVerifyBehaviour);
	}

	@Test
	void shouldCorrectlyHandleRefuse() {
		// when
		behaviour.handleFailure(message);

		// then
		verify(timescaleDatabase).setAdaptationActionAvailability(
				getAdaptationAction(ADAPTATION_ACTION_TYPE).get(0).getActionId(), true);
	}
}
