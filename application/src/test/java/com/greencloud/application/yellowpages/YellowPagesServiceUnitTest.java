package com.greencloud.application.yellowpages;

import static com.greencloud.application.yellowpages.YellowPagesService.prepareSubscription;
import static jade.lang.acl.ACLMessage.SUBSCRIBE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.AID;

class YellowPagesServiceUnitTest {

	@Test
	@DisplayName("Test preparing subscription message for DF")
	void testPrepareSubscription() {
		var mockDF = new AID("test_df", AID.ISGUID);
		var mockAgent = spy(ServerAgent.class);

		doReturn("test_server").when(mockAgent).getName();
		doReturn(mockDF).when(mockAgent).getDefaultDF();

		var expectedContent = "((iota ?x (result (action "
				+ "( agent-identifier :name test_df ) "
				+ "(search (df-agent-description "
				+ ":services (set (service-description :type test_service_type :ownership test_server))) "
				+ "(search-constraints :max-results -1))) ?x)))";

		var result = prepareSubscription(mockAgent, "test_service_type", "test_server");

		assertThat(result)
				.as("Message contains correct fields")
				.satisfies(message -> {
					assertThat(message.getProtocol()).isEqualTo("fipa-subscribe");
					assertThat(message.getOntology()).isEqualTo("FIPA-Agent-Management");
					assertThat(message.getLanguage()).isEqualTo("fipa-sl");
					assertThat(message.getConversationId()).contains("conv-test_server");
					assertThat(message.getReplyWith()).contains("rw-test_server");
					assertThat(message.getContent()).isEqualTo(expectedContent);
					assertThat(message.getPerformative()).isEqualTo(SUBSCRIBE);
					assertThat(message.getAllReceiver().next())
							.isInstanceOfSatisfying(AID.class,
									receiver -> assertThat(receiver.getName()).isEqualTo("test_df"));
				});
	}
}
