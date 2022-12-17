package com.greencloud.application.yellowpages;

import static com.greencloud.application.yellowpages.YellowPagesService.decodeSubscription;
import static com.greencloud.application.yellowpages.YellowPagesService.prepareSubscription;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.SUBSCRIBE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class YellowPagesServiceUnitTest {

	private static final String mockJadeAddress = "192.168.56.1:6996/JADE";
	private static final String mockAgent = "test_agent@" + mockJadeAddress;

	private static Stream<Arguments> parametersSubscriptionMessage() {
		return Stream.of(
				arguments("test_server",
						"((iota ?x (result (action "
								+ "( agent-identifier :name test_df ) "
								+ "(search (df-agent-description "
								+ ":services (set (service-description :type test_service_type :ownership test_server))) "
								+ "(search-constraints :max-results -1))) ?x)))"),
				arguments(null,
						"((iota ?x (result (action "
								+ "( agent-identifier :name test_df ) "
								+ "(search (df-agent-description "
								+ ":services (set (service-description :type test_service_type))) "
								+ "(search-constraints :max-results -1))) ?x)))")
		);
	}

	@Test
	@DisplayName("Test decode subscription")
	void testDecodeSubscription() {
		var mockAddress = ":addresses (sequence http://Test-Address/acc)) ";

		var iotaPrefix = "((= (iota ?x (result (action ";
		var mockDFDescription = "(agent-identifier " + ":name df@" + mockJadeAddress + " " + mockAddress;
		var mockSearch = "(search "
				+ "(df-agent-description :services "
				+ "(set (service-description :type test_type))) "
				+ "(search-constraints :max-results -1))) ?x)) ";

		var mockResult = "(sequence "
				+ "(df-agent-description "
				+ ":name (agent-identifier :name " + mockAgent + " " + mockAddress
				+ ":services (set (service-description "
				+ ":name test_agent "
				+ ":type test_type))))))";

		var messageContent = String.join("", iotaPrefix, mockDFDescription, mockSearch, mockResult);

		final ACLMessage testMessage = new ACLMessage(INFORM);
		testMessage.setContent(messageContent);

		var result = decodeSubscription(testMessage);

		assertThat(result)
				.as("Result has correct size equal to 1")
				.hasSize(1)
				.as("Result has correct content")
				.allMatch(aid -> aid.getName().equals(mockAgent));
	}

	@ParameterizedTest
	@MethodSource("parametersSubscriptionMessage")
	@DisplayName("Test preparing subscription message for DF")
	void testPrepareSubscription(@Nullable String ownership, String expectedContent) {
		var mockDF = new AID("test_df", AID.ISGUID);
		var mockAgent = spy(ServerAgent.class);

		doReturn("test_server").when(mockAgent).getName();
		doReturn(mockDF).when(mockAgent).getDefaultDF();

		var result = Objects.nonNull(ownership) ?
				prepareSubscription(mockAgent, "test_service_type", "test_server") :
				prepareSubscription(mockAgent, "test_service_type");

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
