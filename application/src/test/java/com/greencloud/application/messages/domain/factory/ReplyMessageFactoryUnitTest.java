package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareAcceptReplyWithProtocol;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareInformReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class ReplyMessageFactoryUnitTest {

	AID MOCK_SENDER;
	ACLMessage MOCK_REQUEST;

	@BeforeEach
	void init() {
		prepareTestRequestMessage();
	}

	@Test
	@DisplayName("Test prepare reply")
	void testPrepareReply() {
		final int performative = REFUSE;
		final JobInstanceIdentifier content = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();

		final String expectedContent = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		var result = prepareReply(MOCK_REQUEST, content, performative);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(performative);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	@Test
	@DisplayName("Test prepare string reply")
	void testPrepareStringReply() {
		final int performative = AGREE;
		final String content = "test_content";

		var result = prepareStringReply(MOCK_REQUEST, content, performative);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(performative);
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	@Test
	@DisplayName("Test prepare refuse reply")
	void testPrepareRefuseReply() {
		var result = prepareRefuseReply(MOCK_REQUEST);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(REFUSE);
		assertThat(result.getContent()).isEqualTo("REFUSE");
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	@Test
	@DisplayName("Test prepare failure reply with default content")
	void testPrepareFailureNoContentReply() {
		var result = prepareFailureReply(MOCK_REQUEST);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(FAILURE);
		assertThat(result.getContent()).isEqualTo("FAILURE");
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	@Test
	@DisplayName("Test prepare inform reply")
	void testPrepareInformReply() {
		var result = prepareInformReply(MOCK_REQUEST);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo("INFORM");
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	@Test
	@DisplayName("Test prepare accept reply with protocol")
	void testPrepareAcceptReplyWithProtocol() {
		final JobInstanceIdentifier mockJobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();
		final String protocol = SERVER_JOB_CFP_PROTOCOL;
		MOCK_REQUEST.setPerformative(CFP);

		final String expectedContent =
				"{\"jobInstanceIdentifier\":{\"jobId\":\"1\",\"startTime\":1641043800.000000000},"
						+ "\"replyProtocol\":\"SERVER_JOB_CFP\"}";

		var result = prepareAcceptReplyWithProtocol(MOCK_REQUEST, mockJobInstance, protocol);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(ACCEPT_PROPOSAL);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	@Test
	@DisplayName("Test prepare failure reply message")
	void testPrepareFailureReply() {
		var result = prepareFailureReply(MOCK_REQUEST);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getPerformative()).isEqualTo(FAILURE);
		assertThat(result.getContent()).isEqualTo("FAILURE");
		assertThat(result.getConversationId()).isEqualTo("test_conversationId");
		assertThat(result.getProtocol()).isEqualTo("test_protocol");
		assertThat(receiverIt).allMatch(aid -> aid.equals(MOCK_SENDER));
	}

	private void prepareTestRequestMessage() {
		final AID testAID = mock(AID.class);
		doReturn("test_receiver").when(testAID).getName();

		final ACLMessage testMessage = new ACLMessage(REQUEST);
		testMessage.setProtocol("test_protocol");
		testMessage.setConversationId("test_conversationId");
		testMessage.addReceiver(testAID);

		MOCK_REQUEST = testMessage;
		MOCK_SENDER = testAID;
	}
}
