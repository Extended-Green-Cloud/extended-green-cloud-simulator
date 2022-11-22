package com.greencloud.application.mesages.domain.factory;

import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.messages.domain.factory.JobStatusMessageFactory;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class JobStatusMessageFactoryUnitTest {

	@Test
	@DisplayName("Test prepare job status for CNA message")
	void testPrepareJobStatusMessageForCNA() {
		final AID aid = mock(AID.class);
		doReturn("CNA").when(aid).getName();

		final ServerAgent server = mock(ServerAgent.class);
		doReturn(aid).when(server).getOwnerCloudNetworkAgent();

		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();
		final String expectedContent = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		final ACLMessage result = JobStatusMessageFactory.prepareJobStatusMessageForCNA(jobInstance, DELAYED_JOB_ID,
				server);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(DELAYED_JOB_ID);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid1 -> aid.getName().equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job status finish message")
	void testPrepareJobFinishMessage() {
		final AID aid = mock(AID.class);
		doReturn("Sender").when(aid).getName();
		final String id = "1";
		final Instant start = Instant.parse("2022-01-01T13:30:00.000Z");

		final String expectedResult = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		final ACLMessage result = JobStatusMessageFactory.prepareJobFinishMessage(id, start, singletonList(aid));
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(FINISH_JOB_ID);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedResult);
		assertThat(receiverIt).allMatch(aid1 -> aid.getName().equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job status started message")
	void testPrepareJobStartedMessage() {
		final AID aid = mock(AID.class);
		doReturn("Sender").when(aid).getName();
		final String id = "1";
		final Instant start = Instant.parse("2022-01-01T13:30:00.000Z");

		final String expectedResult = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		final ACLMessage result = JobStatusMessageFactory.prepareJobStartedMessage(id, start, singletonList(aid));
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(STARTED_JOB_ID);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedResult);
		assertThat(receiverIt).allMatch(aid1 -> aid.getName().equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job manual finish message")
	void testPrepareJobManualFinishMessage() {
		final AID aid = mock(AID.class);
		doReturn("Sender").when(aid).getName();

		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();
		final String expectedResult = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		final ACLMessage result = JobStatusMessageFactory.prepareManualFinishMessageForServer(jobInstance, aid);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(MANUAL_JOB_FINISH_PROTOCOL);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedResult);
		assertThat(receiverIt).anyMatch(aid1 -> aid.getName().equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job start status request message")
	void testPrepareJobStartStatusRequestMessage() {
		final AID aid = mock(AID.class);
		doReturn("Sender").when(aid).getName();

		final ACLMessage result = JobStatusMessageFactory.prepareJobStartStatusRequestMessage("1", aid);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(JOB_START_STATUS_PROTOCOL);
		assertThat(result.getContent()).isEqualTo("1");
		assertThat(receiverIt).anyMatch(aid1 -> aid.getName().equals(aid1.getName()));
	}
}
