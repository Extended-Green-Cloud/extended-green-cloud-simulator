package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static jade.core.AID.ISGUID;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

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

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitiateAdaptationActionRequestTest {

	private static final Double GOAL_QUALITY = 0.5;
	private static final AdaptationActionEnum ADAPTATION_ACTION_TYPE = AdaptationActionEnum.ADD_SERVER;
	private static final AID TEST_AID = new AID("test", ISGUID);

	@Mock
	ManagingAgent managingAgent;
	@Mock
	ManagingAgentNode managingAgentNode;
	@Mock
	TimescaleDatabase timescaleDatabase;

	ACLMessage message;
	InitiateAdaptationActionRequest behaviour;

	@Captor
	ArgumentCaptor<VerifyAdaptationActionResult> captor;

	@BeforeEach
	void init() {
		message = new ACLMessage(REQUEST);
		message.addReceiver(TEST_AID);
		message.setConversationId(ADAPTATION_ACTION_TYPE.toString());

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(timescaleDatabase);
		doNothing().when(managingAgentNode).logNewAdaptation(any(),any(),any());

		behaviour = new InitiateAdaptationActionRequest(managingAgent, message, GOAL_QUALITY);

	}

	@Test
	void shouldCorrectlyHandleInform() {
		// given
		var expectedVerifyBehaviour = new VerifyAdaptationActionResult(managingAgent, now(), ADAPTATION_ACTION_TYPE,
				TEST_AID, GOAL_QUALITY);

		// when
		behaviour.handleInform(message);

		// then
		verify(managingAgent).removeBehaviour(behaviour);
		verify(managingAgent).addBehaviour(captor.capture());
		assertThat(captor.getValue())
				.as("Created behaviour should be equal to the expected one")
				.usingRecursiveComparison()
				.ignoringFields("actionTimestamp")
				.isEqualTo(expectedVerifyBehaviour);
	}

	@Test
	void shouldCorrectlyHandleRefuse() {
		// when
		behaviour.handleFailure(message);

		// then
		verify(timescaleDatabase).setAdaptationActionAvailability(
				getAdaptationAction(ADAPTATION_ACTION_TYPE).getActionId(), true);
		verify(managingAgent).removeBehaviour(behaviour);
	}
}
