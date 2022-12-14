package com.greencloud.application.messages.domain.factory;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class ReplyMessageFactoryUnitTest {

	@Test
	@DisplayName("Test prepare failure reply message")
	void testPrepareFailureReply() {
		final AID testAID = mock(AID.class);

		doReturn("test_receiver").when(testAID).getName();

		final ACLMessage testMessage = new ACLMessage(INFORM);
		testMessage.setProtocol("test_protocol");
		testMessage.setConversationId("test_conversationId");
		testMessage.addReceiver(testAID);

		var result = ReplyMessageFactory.prepareFailureReply(testMessage);

		assertThat(result.getPerformative()).isEqualTo(FAILURE);
		assertThat(result.getContent()).isEqualTo("FAILURE");
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(result.getAllReceiver())
				.toIterable()
				.hasSize(1)
				.allMatch((el) -> ((AID) el).getName().equals("test_receiver"));
	}
}
