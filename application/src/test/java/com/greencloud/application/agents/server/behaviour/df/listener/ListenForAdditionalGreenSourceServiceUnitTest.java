package com.greencloud.application.agents.server.behaviour.df.listener;

import static com.greencloud.application.agents.server.behaviour.df.listener.templates.DFServerMessageTemplates.GREEN_SOURCE_CONNECTION_TEMPLATE;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.management.ServerConfigManagement;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class ListenForAdditionalGreenSourceServiceUnitTest {

	@Mock
	private ServerAgent mockServerAgent;
	@Mock
	private ServerConfigManagement mockConfigManagement;

	private ListenForAdditionalGreenSourceService listenForAdditionalGreenSourceService;

	@BeforeEach
	void init() {
		var mockDF = new AID("test_df", AID.ISGUID);

		mockServerAgent = spy(ServerAgent.class);
		mockConfigManagement = spy(new ServerConfigManagement(mockServerAgent));

		doReturn(mockDF).when(mockServerAgent).getDefaultDF();
		doReturn(mockConfigManagement).when(mockServerAgent).manageConfig();

		listenForAdditionalGreenSourceService = new ListenForAdditionalGreenSourceService(mockServerAgent);
	}

	@Test
	@DisplayName("Test receiving new green source service for already existing green source")
	void testActionForExistingGreenSource() {
		final AID testAID = new AID("test_green_source", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(CONNECT_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		doReturn(Map.of(testAID, true)).when(mockServerAgent).getOwnedGreenSources();
		when(mockServerAgent.receive(GREEN_SOURCE_CONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		listenForAdditionalGreenSourceService.action();

		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == REFUSE));
	}

	@Test
	@DisplayName("Test receiving new green source service for non existing green source")
	void testActionForNonExistingGreenSource() {
		final AID testAID = new AID("test_green_source", AID.ISGUID);

		final ACLMessage receivedInfo = new ACLMessage(REQUEST);
		receivedInfo.setProtocol(CONNECT_GREEN_SOURCE_PROTOCOL);
		receivedInfo.setSender(testAID);

		doReturn(new HashMap<>()).when(mockServerAgent).getOwnedGreenSources();
		mockConfigManagement.setWeightsForGreenSourcesMap(new HashMap<>());
		when(mockServerAgent.receive(GREEN_SOURCE_CONNECTION_TEMPLATE)).thenReturn(receivedInfo);

		listenForAdditionalGreenSourceService.action();

		verify(mockServerAgent).send(argThat(msg -> msg.getPerformative() == INFORM));

		assertThat(mockServerAgent.getOwnedGreenSources())
				.hasSize(1)
				.containsKey(testAID);
		assertThat(mockConfigManagement.getWeightsForGreenSourcesMap()).containsEntry(testAID, 1);
	}
}
