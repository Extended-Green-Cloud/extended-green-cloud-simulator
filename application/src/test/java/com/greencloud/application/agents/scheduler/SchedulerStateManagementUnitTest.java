package com.greencloud.application.agents.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.application.domain.job.ImmutableClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class SchedulerStateManagementUnitTest {

	private ConcurrentMap<ClientJob, JobStatusEnum> MOCK_JOBS;
	@Mock
	private SchedulerAgent mockSchedulerAgent;
	private SchedulerStateManagement schedulerStateManagement;

	private static Stream<Arguments> parametersGetById() {
		return Stream.of(Arguments.of("1", true), Arguments.of("10000", false));
	}

	@BeforeEach
	void init() {
		mockSchedulerAgent = spy(SchedulerAgent.class);
		MOCK_JOBS = setUpCloudNetworkJobs();
		schedulerStateManagement = new SchedulerStateManagement(mockSchedulerAgent);

		doReturn(MOCK_JOBS).when(mockSchedulerAgent).getClientJobs();
		doReturn(schedulerStateManagement).when(mockSchedulerAgent).manage();
		doReturn(new SchedulerConfigurationManagement(0.7, 0.3, 10)).when(mockSchedulerAgent).config();
	}

	@ParameterizedTest
	@MethodSource("parametersGetById")
	@DisplayName("Test getting job by id")
	void testGettingJobById(final String jobId, final boolean result) {
		final ClientJob jobResult = schedulerStateManagement.getJobById(jobId);
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@Test
	@DisplayName("Test postponing job for job after deadline")
	void testPostponeJobExecutionAfterDeadline() {
		final ClientJob mockJob1 = ImmutableClientJob.builder()
				.jobId("1")
				.clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T10:00:00.000Z"))
				.power(10)
				.build();
		assertThat(schedulerStateManagement.postponeJobExecution(mockJob1)).isFalse();
	}

	@Test
	@DisplayName("Test postponing job for full job queue")
	void testPostponeJobExecutionFullQueue() {
		final Comparator<ClientJob> testComparator = Comparator.comparingDouble(
				job -> mockSchedulerAgent.config().getJobPriority(job));
		final ClientJob mockJob1 = ImmutableClientJob.builder()
				.jobId("1")
				.clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T15:00:00.000Z"))
				.power(10)
				.build();
		final ClientJob mockJob2 = ImmutableClientJob.builder()
				.jobId("2")
				.clientIdentifier("Client2")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T15:00:00.000Z"))
				.power(10)
				.build();
		doReturn(new PriorityBlockingQueue<>(1, testComparator)).when(mockSchedulerAgent).getJobsToBeExecuted();
		assertThat(schedulerStateManagement.postponeJobExecution(mockJob1)).isTrue();
		assertThat(schedulerStateManagement.postponeJobExecution(mockJob2)).isTrue();

	}

	/**
	 * Class creates mock scheduler jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * Job1 -> power: 10, time: 08:00 - 10:00, status: PROCESSING,
	 * Job2 -> power: 20, time: 07:00 - 11:00, status: ACCEPTED
	 */
	private ConcurrentMap<ClientJob, JobStatusEnum> setUpCloudNetworkJobs() {
		final ClientJob mockJob1 = ImmutableClientJob.builder()
				.jobId("1")
				.clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();
		final ClientJob mockJob2 = ImmutableClientJob.builder()
				.jobId("2")
				.clientIdentifier("Client2")
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20)
				.build();
		final ConcurrentMap<ClientJob, JobStatusEnum> mockJobMap = new ConcurrentHashMap<>();
		mockJobMap.put(mockJob1, JobStatusEnum.PROCESSING);
		mockJobMap.put(mockJob2, JobStatusEnum.ACCEPTED);
		return mockJobMap;
	}
}
