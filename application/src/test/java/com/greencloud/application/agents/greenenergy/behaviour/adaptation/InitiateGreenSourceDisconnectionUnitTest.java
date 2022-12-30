package com.greencloud.application.agents.greenenergy.behaviour.adaptation;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.domain.GreenSourceDisconnection;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class InitiateGreenSourceDisconnectionUnitTest {

	@Mock
	private GreenEnergyAgent greenEnergyAgent;
	@Mock
	private GreenEnergyAgentNode mockNode;
	@Mock
	private GreenEnergyAdaptationManagement greenEnergyAdaptationManagement;
	@Mock
	private ACLMessage mockAdaptationRequest;

	private InitiateGreenSourceDisconnection initiateGreenSourceDisconnection;

	@BeforeEach
	void init() {
		mockNode = mock(GreenEnergyAgentNode.class);
		mockAdaptationRequest = spy(new ACLMessage(REQUEST));
		greenEnergyAgent = spy(GreenEnergyAgent.class);
		greenEnergyAdaptationManagement = spy(new GreenEnergyAdaptationManagement(greenEnergyAgent));
		greenEnergyAgent.setAdaptationManagement(greenEnergyAdaptationManagement);

		doReturn(mockNode).when(greenEnergyAgent).getAgentNode();

		var testDisconnection = new GreenSourceDisconnection(null, mockAdaptationRequest, true);
		greenEnergyAdaptationManagement.setGreenSourceDisconnection(testDisconnection);
	}

	@Test
	@DisplayName("Test handle refuse message")
	void testRefuse() {
		var server = new AID("test_server1", AID.ISGUID);
		initiateGreenSourceDisconnection = InitiateGreenSourceDisconnection.create(greenEnergyAgent, server);

		var refuse = new ACLMessage(REFUSE);
		refuse.setSender(server);

		clearInvocations(greenEnergyAgent);
		clearInvocations(greenEnergyAdaptationManagement);

		initiateGreenSourceDisconnection.handleRefuse(refuse);

		verify(greenEnergyAdaptationManagement).getGreenSourceDisconnectionState();
		verify(greenEnergyAgent).send(argThat(message -> message.getPerformative() == FAILURE));

		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState().isBeingDisconnected()).isFalse();
		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()
				.getServerToBeDisconnected()).isNull();
		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()
				.getOriginalAdaptationMessage()).isNull();
	}

	@Test
	@DisplayName("Test handle inform message")
	void testInform() {
		var server = new AID("test_server1@192.168.56.1:6996/JADE", AID.ISGUID);
		initiateGreenSourceDisconnection = InitiateGreenSourceDisconnection.create(greenEnergyAgent, server);

		var inform = new ACLMessage(INFORM);
		inform.setSender(server);

		clearInvocations(greenEnergyAgent);
		clearInvocations(greenEnergyAdaptationManagement);

		initiateGreenSourceDisconnection.handleInform(inform);

		verify(greenEnergyAdaptationManagement).getGreenSourceDisconnectionState();
		verify(greenEnergyAgent).send(argThat(message -> message.getPerformative() == INFORM));
		verify(mockNode).updateServerConnection("test_server1", false);

		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState().isBeingDisconnected()).isFalse();
		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()
				.getServerToBeDisconnected()).isNull();
		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()
				.getOriginalAdaptationMessage()).isNull();
	}
}
