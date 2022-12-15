package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAdjustmentMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAnnouncementMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.ImmutableJobTimeFrames;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobTimeFrames;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class JobStatusMessageFactoryUnitTest {

	@Test
	@DisplayName("Test prepare job status message for CNA")
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
	@DisplayName("Test prepare job status message for Client with String as content")
	void testPrepareJobStatusMessageForClient() {
		final String client = "test_client";
		final String content = "test_content";
		final String conversationId = STARTED_JOB_ID;

		final ACLMessage result = prepareJobStatusMessageForClient(client, content, conversationId);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(conversationId);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(receiverIt).allMatch(aid1 -> client.equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job status message for Client with Object as content")
	void testPrepareJobStatusMessageForClientWithObject() {
		final String client = "test_client";
		final JobTimeFrames content = ImmutableJobTimeFrames.builder()
				.jobId("1")
				.newJobStart(Instant.parse("2022-01-01T13:30:00.000Z"))
				.newJobEnd(Instant.parse("2022-01-01T14:30:00.000Z"))
				.build();
		final String conversationId = RE_SCHEDULED_JOB_ID;
		final String expectedContent =
				"{\"newJobStart\":1641043800.000000000,\"newJobEnd\":1641047400.000000000,\"jobId\":\"1\"}";

		final ACLMessage result = prepareJobStatusMessageForClient(client, content, conversationId);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(conversationId);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid1 -> client.equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job status message for Scheduler")
	void testPrepareJobStatusMessageForScheduler() {
		final AID mockScheduler = mock(AID.class);
		doReturn("test_scheduler").when(mockScheduler).getName();

		final CloudNetworkAgent mockCloudNetwork = mock(CloudNetworkAgent.class);
		doReturn(mockScheduler).when(mockCloudNetwork).getScheduler();

		final String testJobId = "1";
		final String conversationId = FINISH_JOB_ID;

		final ACLMessage result = prepareJobStatusMessageForScheduler(mockCloudNetwork, testJobId, conversationId);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(conversationId);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(testJobId);
		assertThat(receiverIt).allMatch(aid1 -> "test_scheduler".equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare job announcement message")
	void testPrepareJobAnnouncementMessage() {
		final AID mockScheduler = mock(AID.class);
		doReturn("test_scheduler").when(mockScheduler).getName();

		final ClientJob mockJob = ImmutableClientJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T16:30:00.000Z"))
				.clientIdentifier("test_client")
				.power(10)
				.build();

		final String expectedContent =
				"{\"jobId\":\"1\","
						+ "\"startTime\":1641043800.000000000,"
						+ "\"endTime\":1641047400.000000000,"
						+ "\"deadline\":1641054600.000000000,"
						+ "\"power\":10,"
						+ "\"clientIdentifier\":\"test_client\"}";

		final ACLMessage result = prepareJobAnnouncementMessage(mockScheduler, mockJob);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(ANNOUNCED_JOB_PROTOCOL);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockScheduler));
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

	@Test
	@DisplayName("Test prepare job adjustment message")
	void testPrepareJobAdjustmentMessage() {
		final String client = "test_client";
		final ClientJob adjustedJob = ImmutableClientJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T16:30:00.000Z"))
				.clientIdentifier("test_client")
				.power(10)
				.build();
		final String expectedResult =
				"{\"newJobStart\":1641043800.000000000,\"newJobEnd\":1641047400.000000000,\"jobId\":\"1\"}";

		final ACLMessage result = prepareJobAdjustmentMessage(client, adjustedJob);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(RE_SCHEDULED_JOB_ID);
		assertThat(result.getContent()).isEqualTo(expectedResult);
		assertThat(receiverIt).anyMatch(aid1 -> client.equals(aid1.getName()));
	}
}
