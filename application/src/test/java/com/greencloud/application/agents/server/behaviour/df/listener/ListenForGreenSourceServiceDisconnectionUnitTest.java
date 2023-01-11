package com.greencloud.application.agents.server.behaviour.df.listener;

import static com.greencloud.application.agents.server.behaviour.df.listener.templates.DFServerMessageTemplates.GREEN_SOURCE_DISCONNECTION_TEMPLATE;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DEACTIVATE_GREEN_SOURCE_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DISCONNECT_GREEN_SOURCE_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.management.ServerConfigManagement;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ListenForGreenSourceServiceDisconnectionUnitTest {

	@Mock
	private ServerAgent mockServerAgent;
	@Mock
	private ServerConfigManagement mockConfigManagement;

	private ListenForGreenSourceServiceDisconnection listenForGreenSourceServiceDisconnection;

	@BeforeEach
	void init() {
		mockServerAgent = spy(ServerAgent.class);
		mockConfigManagement = spy(new ServerConfigManagement(mockServerAgent));

		doReturn(mockConfigManagement).when(mockServerAgent).manageConfig();
		doReturn(new HashMap<>()).when(mockServerAgent).getGreenSourceForJobMap();
		prepareOwnedGreenSources();

		listenForGreenSourceServiceDisconnection = new ListenForGreenSourceServiceDisconnection(mockServerAgent);
	}

	@Test
	@DisplayName("Test handle deactivate green source request for not connected")
	void testHandleGreenSourceDeactivationForNotConnected() {
		final AID testAID = new AID("test_gs4", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(DEACTIVATE_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		when(mockServerAgent.receive(GREEN_SOURCE_DISCONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		clearInvocations(mockConfigManagement);
		clearInvocations(mockServerAgent);

		listenForGreenSourceServiceDisconnection.action();

		verify(mockServerAgent).getOwnedGreenSources();
		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == REFUSE));
	}

	@Test
	@DisplayName("Test handle deactivate green source request green source connected")
	void testHandleGreenSourceDeactivationForConnected() {
		final AID testAID = new AID("test_gs1", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(DEACTIVATE_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		when(mockServerAgent.receive(GREEN_SOURCE_DISCONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		clearInvocations(mockConfigManagement);
		clearInvocations(mockServerAgent);

		listenForGreenSourceServiceDisconnection.action();

		verify(mockServerAgent, times(2)).getOwnedGreenSources();
		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == INFORM));

		assertThat(mockServerAgent.getOwnedGreenSources().get(testAID)).isFalse();
	}

	@Test
	@DisplayName("Test handle green source disconnection request for not connected")
	void testHandleGreenSourceDisconnectionForNotConnected() {
		final AID testAID = new AID("test_gs4", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		when(mockServerAgent.receive(GREEN_SOURCE_DISCONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		clearInvocations(mockConfigManagement);
		clearInvocations(mockServerAgent);

		listenForGreenSourceServiceDisconnection.action();

		verify(mockServerAgent).getOwnedGreenSources();
		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == REFUSE));
	}

	@Test
	@DisplayName("Test handle green source disconnection request for not deactivated")
	void testHandleGreenSourceDisconnectionForNotDeactivated() {
		final AID testAID = new AID("test_gs1", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		when(mockServerAgent.receive(GREEN_SOURCE_DISCONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		clearInvocations(mockConfigManagement);
		clearInvocations(mockServerAgent);

		listenForGreenSourceServiceDisconnection.action();

		verify(mockServerAgent, times(2)).getOwnedGreenSources();
		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == REFUSE));
	}

	@Test
	@DisplayName("Test handle green source disconnection request for active jobs")
	void testHandleGreenSourceDisconnectionForActiveJobs() {
		final AID testAID = new AID("test_gs2", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		mockServerAgent.getGreenSourceForJobMap().put("1", testAID);

		when(mockServerAgent.receive(GREEN_SOURCE_DISCONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		clearInvocations(mockConfigManagement);
		clearInvocations(mockServerAgent);

		listenForGreenSourceServiceDisconnection.action();

		verify(mockServerAgent, times(2)).getOwnedGreenSources();
		verify(mockServerAgent).getGreenSourceForJobMap();
		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == REFUSE));
	}

	@Test
	@DisplayName("Test handle green source disconnection request")
	void testHandleGreenSourceDisconnection() {
		final AID testAID = new AID("test_gs2", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		when(mockServerAgent.receive(GREEN_SOURCE_DISCONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		clearInvocations(mockConfigManagement);
		clearInvocations(mockServerAgent);

		listenForGreenSourceServiceDisconnection.action();

		verify(mockServerAgent, times(3)).getOwnedGreenSources();
		verify(mockServerAgent).getGreenSourceForJobMap();
		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == INFORM));

		assertThat(mockServerAgent.getOwnedGreenSources()).doesNotContainKey(testAID);
	}

	void prepareOwnedGreenSources() {
		var testGreenSources = new HashMap<>(Map.of(
				new AID("test_gs1", AID.ISGUID), true,
				new AID("test_gs2", AID.ISGUID), false,
				new AID("test_gs3", AID.ISGUID), true
		));

		doReturn(testGreenSources).when(mockServerAgent).getOwnedGreenSources();
	}
}
