package org.greencloud.managingsystem.agent.behaviour.executor;

import static jade.core.AID.ISGUID;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
class InitiateAdaptationActionRequestTest {

	private static final Double GOAL_QUALITY = 0.5;
	private static final String ADAPTATION_ACTION_ID = "1";
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
		message.setConversationId(ADAPTATION_ACTION_ID);
		behaviour = new InitiateAdaptationActionRequest(managingAgent, message, GOAL_QUALITY);

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(timescaleDatabase);
	}

	@Test
	void shouldCorrectlyHandleInform() {
		// given
		var expectedVerifyBehaviour = new VerifyAdaptationActionResult(managingAgent, now(), ADAPTATION_ACTION_ID,
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
		verify(timescaleDatabase).setAdaptationActionAvailability(Integer.parseInt(ADAPTATION_ACTION_ID), true);
		verify(managingAgent).removeBehaviour(behaviour);
	}
}
