package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SPLIT_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAdjustmentMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAnnouncementMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.preparePostponeJobMessageForClient;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareSplitJobMessageForClient;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.useMockTime;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.application.domain.job.SplitJob;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class JobStatusMessageFactoryUnitTest {

	@BeforeEach
	void init() {
		useMockTime(Instant.parse("2022-01-01T13:30:00.000Z"), ZoneId.of("UTC"));
	}

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
		final String expectedResult =
				"{\"jobInstance\":{\"jobId\":\"1\",\"startTime\":1641043800.000000000},"
						+ "\"changeTime\":1641043800.000000000}";

		final ACLMessage result = JobStatusMessageFactory.prepareJobStatusMessageForCNA(jobInstance, DELAYED_JOB_ID,
				server);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(DELAYED_JOB_ID);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedResult);
		assertThat(receiverIt).allMatch(aid1 -> aid.getName().equals(aid1.getName()));
	}

	@ParameterizedTest
	@MethodSource("parametersForJobStatusUpdateMessage")
	@DisplayName("Test prepare job status message for Client with JobStatusUpdate as content")
	void testPrepareJobStatusMessageForClient(final Object content, boolean useJob) {
		final String client = "test_client";
		final String conversationId = STARTED_JOB_ID;

		var result = useJob ?
				prepareJobStatusMessageForClient((ClientJob) content, conversationId) :
				prepareJobStatusMessageForClient(client, (JobStatusUpdate) content, conversationId);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		final String expectedResult =
				"{\"jobInstance\":{\"jobId\":\"1\",\"startTime\":1641043800.000000000},"
						+ "\"changeTime\":1641043800.000000000}";

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(conversationId);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedResult);
		assertThat(receiverIt).allMatch(aid1 -> client.equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare split job message for client")
	void testPrepareSplitJobMessageForClient() {
		final String client = "test_client";
		var jobPart1 = ImmutableClientJob.builder()
				.jobId("1#part1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T15:30:00.000Z"))
				.power(10)
				.clientIdentifier("test_client")
				.build();
		var jobPart2 = ImmutableClientJob.builder()
				.jobId("1#part2")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T15:30:00.000Z"))
				.power(10)
				.clientIdentifier("test_client")
				.build();
		final SplitJob content = new SplitJob(List.of(jobPart1, jobPart2));

		final ACLMessage result = prepareSplitJobMessageForClient(client, content);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		final String expectedContent = "{\"jobParts\":["
				+ "{\"jobId\":\"1#part1\",\"startTime\":1641043800.000000000,\"endTime\":1641047400.000000000,"
				+ "\"deadline\":1641051000.000000000,\"power\":10,\"clientIdentifier\":\"test_client\"}"
				+ ","
				+ "{\"jobId\":\"1#part2\",\"startTime\":1641043800.000000000,\"endTime\":1641047400.000000000,"
				+ "\"deadline\":1641051000.000000000,\"power\":10,\"clientIdentifier\":\"test_client\"}"
				+ "]}";

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(SPLIT_JOB_ID);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid1 -> client.equals(aid1.getName()));
	}

	@Test
	@DisplayName("Test prepare postpone job message for client")
	void testPreparePostponeJobMessageForClient() {
		final String client = "test_client";
		var jobPart1 = ImmutableClientJob.builder()
				.jobId("1#part1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T15:30:00.000Z"))
				.power(10)
				.clientIdentifier("test_client")
				.build();
		final String expectedContent = "1#part1";

		final ACLMessage result = preparePostponeJobMessageForClient(jobPart1);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(POSTPONED_JOB_ID);
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

		final JobStatusUpdate jobStatusUpdate = new JobStatusUpdate(ImmutableJobInstanceIdentifier.builder()
				.jobId("1").startTime(Instant.parse("2022-01-01T13:30:00.000Z")).build(),
				Instant.parse("2022-01-01T13:30:00.000Z"));
		final String conversationId = FINISH_JOB_ID;

		final ACLMessage result = prepareJobStatusMessageForScheduler(mockCloudNetwork, jobStatusUpdate,
				conversationId);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		final String expectedResult =
				"{\"jobInstance\":{\"jobId\":\"1\",\"startTime\":1641043800.000000000},"
						+ "\"changeTime\":1641043800.000000000}";

		assertThat(result.getProtocol()).isEqualTo(CHANGE_JOB_STATUS_PROTOCOL);
		assertThat(result.getConversationId()).isEqualTo(conversationId);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedResult);
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

		final String expectedResult =
				"{\"jobInstance\":{\"jobId\":\"1\",\"startTime\":1641043800.000000000},"
						+ "\"changeTime\":1641043800.000000000}";

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

		final String expectedResult =
				"{\"jobInstance\":{\"jobId\":\"1\",\"startTime\":1641043800.000000000},"
						+ "\"changeTime\":1641043800.000000000}";

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

	private static Stream<Arguments> parametersForJobStatusUpdateMessage() {
		var jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();
		var job = ImmutableClientJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T15:30:00.000Z"))
				.power(10)
				.clientIdentifier("test_client")
				.build();

		return Stream.of(
				arguments(new JobStatusUpdate(jobInstance, getCurrentTime()), false),
				arguments(job, true)
		);
	}
}
