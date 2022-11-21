package com.greencloud.application.utils;

import static com.greencloud.application.domain.job.JobStatusEnum.CREATED;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTimeMock;
import static com.greencloud.application.utils.TimeUtils.useMockTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;
import com.greencloud.commons.job.ImmutablePowerJob;
import com.greencloud.commons.job.PowerJob;

class JobUtilsUnitTest {

	// TEST PARAMETERS

	private static Stream<Arguments> parametersGetById() {
		return Stream.of(Arguments.of("1", true), Arguments.of("10000", false));
	}

	private static Stream<Arguments> parametersGetByIdAndStart() {
		return Stream.of(Arguments.of(Instant.parse("2022-01-01T07:00:00.000Z"), "2", true),
				Arguments.of(Instant.parse("2022-01-01T04:30:00.000Z"), "1", false));
	}

	private static Stream<Arguments> parametersGetByIdAndStartInstant() {
		return Stream.of(Arguments.of(
				ImmutableJobInstanceIdentifier.builder().startTime(Instant.parse("2022-01-01T07:00:00.000Z")).jobId("2")
						.build(), true), Arguments.of(
				ImmutableJobInstanceIdentifier.builder().startTime(Instant.parse("2022-01-01T06:00:00.000Z")).jobId("1")
						.build(), false));
	}

	private static Stream<Arguments> parametersIsJobUnique() {
		return Stream.of(Arguments.of("5", false), Arguments.of("3", true), Arguments.of("1", false));
	}

	// SETUP

	@BeforeEach
	void setUp() {
		useMockTime(Instant.parse("2022-01-01T09:00:00.000Z"), ZoneId.of("UTC"));
		setSystemStartTimeMock(Instant.parse("2022-01-01T05:00:00.000Z"));
	}

	// TESTS

	@ParameterizedTest
	@MethodSource("parametersGetById")
	@DisplayName("Test getting job by id")
	void testGettingJobById(final String jobId, final boolean result) {
		final ClientJob mockJob1 = ImmutableClientJob.builder()
				.jobId("1")
				.clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();

		final ClientJob jobResult = JobUtils.getJobById(jobId, Map.of(mockJob1, IN_PROGRESS));
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@ParameterizedTest
	@MethodSource("parametersGetByIdAndStart")
	@DisplayName("Test getting power job by id and start time")
	void testGettingJobByIdAndStartTime(final Instant startTime, final String jobId, final boolean result) {
		final PowerJob mockJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();

		final PowerJob jobResult = JobUtils.getJobByIdAndStartDate(jobId, startTime,
				Map.of(mockJob1, CREATED, mockJob2, IN_PROGRESS));
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@ParameterizedTest
	@MethodSource("parametersGetByIdAndStartInstant")
	@DisplayName("Test getting power job by id and start time instant")
	void testGettingJobByIdAndStartTimeInstant(final JobInstanceIdentifier jobInstance, final boolean result) {
		final PowerJob mockJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();

		final PowerJob jobResult = JobUtils.getJobByIdAndStartDate(jobInstance,
				Map.of(mockJob1, CREATED, mockJob2, IN_PROGRESS));
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@Test
	@DisplayName("Test get jobs timetable with repeatable time instances")
	void testGetJobsTimetableRepeatableInstances() {
		final PowerJob mockCandidatePowerJob = ImmutablePowerJob.builder().jobId("6").power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.build();
		final List<Instant> result = JobUtils.getJobsTimetable(mockCandidatePowerJob, setUpMockJobs());

		assertThat(result).hasSize(8)
				.contains(convertToRealTime(Instant.parse("2022-01-01T13:00:00.000Z")))
				.contains(convertToRealTime(Instant.parse("2022-01-01T12:00:00.000Z")));
	}

	@Test
	@DisplayName("Test get jobs timetable with job in processing")
	void testGetJobsTimetableJobInProcessing() {
		final PowerJob mockCandidatePowerJob = ImmutablePowerJob.builder().jobId("6").power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.build();
		final PowerJob jobProcessing = ImmutablePowerJob.builder().jobId("10")
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final Map<PowerJob, JobStatusEnum> testJobs = setUpMockJobs();
		testJobs.put(jobProcessing, JobStatusEnum.PROCESSING);
		final List<Instant> result = JobUtils.getJobsTimetable(mockCandidatePowerJob, testJobs);

		assertThat(result).hasSize(8)
				.contains(convertToRealTime(Instant.parse("2022-01-01T13:00:00.000Z")))
				.contains(convertToRealTime(Instant.parse("2022-01-01T12:00:00.000Z")))
				.doesNotContain(convertToRealTime(Instant.parse("2022-01-01T13:30:00.000Z")));
	}

	@Test
	@DisplayName("Test getting expected job end time for current time before")
	void testCalculateExpectedJobEndTime() {
		final PowerJob mockJob = ImmutablePowerJob.builder().jobId("6").power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.build();
		final Date expectedResult = Date.from(Instant.parse("2022-01-01T14:00:00.500Z"));

		final Date result = JobUtils.calculateExpectedJobEndTime(mockJob);

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Test getting expected job end time for current time after")
	void testCalculateExpectedJobEndTimeCurrentTimeAfter() {
		useMockTime(Instant.parse("2022-01-01T19:00:00.000Z"), ZoneId.of("UTC"));

		final PowerJob mockJob = ImmutablePowerJob.builder().jobId("6").power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.build();
		final Date expectedResult = Date.from(Instant.parse("2022-01-01T19:00:00.500Z"));

		final Date result = JobUtils.calculateExpectedJobEndTime(mockJob);

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Test getting current job instance not found")
	void testGettingCurrentJobInstanceNotFound() {
		useMockTime(Instant.parse("2022-01-01T19:00:00.000Z"), ZoneId.of("UTC"));
		final Map.Entry<PowerJob, JobStatusEnum> result = JobUtils.getCurrentJobInstance("1", setUpMockJobs());

		assertNull(result);
	}

	@Test
	@DisplayName("Test getting current job instance one instance")
	void testGettingCurrentJobInstanceOneInstance() {
		useMockTime(Instant.parse("2022-01-01T14:00:00.000Z"), ZoneId.of("UTC"));
		final Map.Entry<PowerJob, JobStatusEnum> result = JobUtils.getCurrentJobInstance("2", setUpMockJobs());

		assertNotNull(result);
		assertThat(result.getKey().getDeadline()).isEqualTo(Instant.parse("2022-01-01T20:00:00.000Z"));
		assertThat(result.getKey().getEndTime()).isEqualTo(Instant.parse("2022-01-01T15:00:00.000Z"));
	}

	@Test
	@DisplayName("Test getting current job instance two instances")
	void testGettingCurrentJobInstanceTwoInstances() {
		useMockTime(Instant.parse("2022-01-01T12:00:00.000Z"), ZoneId.of("UTC"));
		final PowerJob jobProcessing = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20)
				.build();
		final Map<PowerJob, JobStatusEnum> testJobs = setUpMockJobs();
		testJobs.put(jobProcessing, IN_PROGRESS);

		final Map.Entry<PowerJob, JobStatusEnum> result = JobUtils.getCurrentJobInstance("1", testJobs);

		assertNotNull(result);
		assertThat(result.getKey().getPower()).isEqualTo(20);
		assertThat(result.getKey().getStartTime()).isEqualTo(Instant.parse("2022-01-01T10:30:00.000Z"));
	}

	@ParameterizedTest
	@MethodSource("parametersIsJobUnique")
	@DisplayName("Test is job unique by id")
	void testIsJobUnique(final String jobId, final boolean result) {
		final PowerJob mockJob = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();
		final Map<PowerJob, JobStatusEnum> testJobs = setUpMockJobs();
		testJobs.put(mockJob, IN_PROGRESS);

		assertThat(JobUtils.isJobUnique(jobId, testJobs)).isEqualTo(result);
	}

	// MOCK DATA

	/**
	 * Class creates mock jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * PowerJob1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
	 * PowerJob2 -> power: 50,  time: 06:00 - 15:00, status: ON_HOLD
	 * PowerJob3 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
	 */
	private Map<PowerJob, JobStatusEnum> setUpMockJobs() {
		final PowerJob mockJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(50).build();
		final PowerJob mockJob3 = ImmutablePowerJob.builder().jobId("3")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(25).build();
		final Map<PowerJob, JobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, JobStatusEnum.ON_HOLD_PLANNED);
		mockJobMap.put(mockJob3, JobStatusEnum.ACCEPTED);
		return mockJobMap;
	}
}
