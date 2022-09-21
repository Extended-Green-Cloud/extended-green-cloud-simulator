package com.greencloud.application.agents.cloudnetwork.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
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

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.domain.job.ImmutableJob;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobStatusEnum;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CloudNetworkStateManagementUnitTest {

	// MOCK OBJECTS

	private Map<Job, JobStatusEnum> MOCK_JOBS;

	@Mock
	private CloudNetworkAgent mockCloudNetwork;
	private CloudNetworkStateManagement cloudNetworkStateManagement;

	// PARAMETERS USED IN TESTS

	private static Stream<Arguments> parametersGetById() {
		return Stream.of(Arguments.of("1", true), Arguments.of("10000", false));
	}

	// TEST SET-UP

	@BeforeAll
	static void setUpAll() {
		AbstractAgent.disableGui();
	}

	@BeforeEach
	void init() {
		MOCK_JOBS = setUpCloudNetworkJobs();
		cloudNetworkStateManagement = new CloudNetworkStateManagement(mockCloudNetwork);

		doReturn(MOCK_JOBS).when(mockCloudNetwork).getNetworkJobs();
	}

	// TESTS

	@Test
	@DisplayName("Test get current power in use")
	void testGetCurrentPowerInUser() {
		assertThat(cloudNetworkStateManagement.getCurrentPowerInUse()).isEqualTo(30);
	}

	@ParameterizedTest
	@MethodSource("parametersGetById")
	@DisplayName("Test getting job by id")
	void testGettingJobById(final String jobId, final boolean result) {
		final Job jobResult = cloudNetworkStateManagement.getJobById(jobId);
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@Test
	@DisplayName("Test incrementing started jobs")
	void testIncrementingStartedJobs() {
		assertThat(cloudNetworkStateManagement.getStartedJobs().get()).isZero();
		cloudNetworkStateManagement.incrementStartedJobs("1");
		assertThat(cloudNetworkStateManagement.getStartedJobs().get()).isOne();
	}

	@Test
	@DisplayName("Test incrementing finished jobs")
	void testIncrementingFinishedJobs() {
		assertThat(cloudNetworkStateManagement.getFinishedJobs().get()).isZero();
		cloudNetworkStateManagement.incrementFinishedJobs("1");
		assertThat(cloudNetworkStateManagement.getFinishedJobs().get()).isOne();
	}

	// PREPARING TEST DATA

	/**
	 * Class creates mock cloud network jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * Job1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
	 * Job2 -> power: 20, time: 07:00 - 11:00, status: IN_PROGRESS
	 * Job3 -> power: 50,  time: 06:00 - 15:00, status: ACCEPTED
	 */
	private Map<Job, JobStatusEnum> setUpCloudNetworkJobs() {
		final Job mockJob1 = ImmutableJob.builder()
				.jobId("1")
				.clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.power(10)
				.build();
		final Job mockJob2 = ImmutableJob.builder()
				.jobId("2")
				.clientIdentifier("Client2")
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.power(20)
				.build();
		final Job mockJob3 = ImmutableJob.builder()
				.jobId("3")
				.clientIdentifier("Client3")
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.power(50)
				.build();
		final Map<Job, JobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob3, JobStatusEnum.ACCEPTED);
		return mockJobMap;
	}
}
