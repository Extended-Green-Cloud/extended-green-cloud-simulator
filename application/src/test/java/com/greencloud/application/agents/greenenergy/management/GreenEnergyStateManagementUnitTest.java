package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.constants.CacheTestConstants.MOCK_WEATHER;
import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_TRANSFER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.ImmutablePowerJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class GreenEnergyStateManagementUnitTest {

	// MOCK OBJECTS

	private static final double MOCK_PRICE = 10;
	private static final int MOCK_CAPACITY = 100;
	private static Map<PowerJob, JobStatusEnum> MOCK_POWER_JOBS;

	@Mock
	private static GreenEnergyAgent mockGreenEnergyAgent;
	@Mock
	private static GreenEnergyStateManagement MOCK_MANAGEMENT;
	@Mock
	private static GreenPowerManagement MOCK_POWER_MANAGEMENT;

	// PARAMETERS USED IN PARAMETRIZED TESTS

	private static Stream<Arguments> parametersGetByIdAndStart() {
		return Stream.of(
				Arguments.of(Instant.parse("2022-01-01T07:00:00.000Z"), "2", true),
				Arguments.of(Instant.parse("2022-01-01T04:30:00.000Z"), "1", false));
	}

	private static Stream<Arguments> parametersGetByIdAndStartInstant() {
		return Stream.of(
				Arguments.of(ImmutableJobInstanceIdentifier.builder()
						.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
						.jobId("3")
						.build(), true),
				Arguments.of(ImmutableJobInstanceIdentifier.builder()
						.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
						.jobId("1")
						.build(), false));
	}

	private static Stream<Arguments> parametersGetById() {
		return Stream.of(Arguments.of("5", true), Arguments.of("10000", false));
	}

	// TEST SET-UP

	@BeforeAll
	static void setUpAll() {
		AbstractAgent.disableGui();
	}

	@BeforeEach
	void init() {
		MOCK_POWER_JOBS = setUpGreenEnergyJobs();
		setUpGreenEnergyMock();
	}

	// TESTS

	@ParameterizedTest
	@MethodSource("parametersGetByIdAndStart")
	@DisplayName("Test getting power job by id and start time")
	void testGettingJobByIdAndStartTime(final Instant startTime, final String jobId, final boolean result) {
		final PowerJob jobResult = mockGreenEnergyAgent.manage().getJobByIdAndStartDate(jobId, startTime);
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@ParameterizedTest
	@MethodSource("parametersGetByIdAndStartInstant")
	@DisplayName("Test getting power job by id and start time instant")
	void testGettingJobByIdAndStartTimeInstant(final JobInstanceIdentifier jobInstance, final boolean result) {
		final PowerJob jobResult = mockGreenEnergyAgent.manage().getJobByIdAndStartDate(jobInstance);
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@ParameterizedTest
	@MethodSource("parametersGetById")
	@DisplayName("Test getting power job by id")
	void testGettingJobById(final String jobId, final boolean result) {
		final PowerJob jobResult = mockGreenEnergyAgent.manage().getJobById(jobId);
		assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
	}

	@Test
	@DisplayName("Test increment started unique power job")
	void testIncrementStartedUniqueJob() {
		final String jobId = "1";

		mockGreenEnergyAgent.manage().incrementStartedJobs(jobId);
		assertThat(MOCK_MANAGEMENT.getUniqueStartedJobs().get()).isEqualTo(1);
		assertThat(MOCK_MANAGEMENT.getStartedJobsInstances().get()).isEqualTo(1);
	}

	@Test
	@DisplayName("Test increment started non unique power job")
	void testIncrementStartedNonUniqueJob() {
		final PowerJob jobProcessing = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.power(10)
				.build();
		mockGreenEnergyAgent.getPowerJobs().put(jobProcessing, JobStatusEnum.IN_PROGRESS);
		final String jobId = "1";

		mockGreenEnergyAgent.manage().incrementStartedJobs(jobId);
		assertThat(MOCK_MANAGEMENT.getUniqueStartedJobs().get()).isZero();
		assertThat(MOCK_MANAGEMENT.getStartedJobsInstances().get()).isEqualTo(1);
	}

	@Test
	@DisplayName("Test increment finished unique power job")
	void testIncrementFinishedUniquePowerJob() {
		final String jobId = "1";

		mockGreenEnergyAgent.manage().incrementFinishedJobs(jobId);
		assertThat(MOCK_MANAGEMENT.getUniqueFinishedJobs().get()).isEqualTo(1);
		assertThat(MOCK_MANAGEMENT.getFinishedJobsInstances().get()).isEqualTo(1);
	}

	@Test
	@DisplayName("Test increment finished non unique power job")
	void testIncrementFinishedNonUniquePowerJob() {
		final PowerJob jobProcessing = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.power(10)
				.build();
		mockGreenEnergyAgent.getPowerJobs().put(jobProcessing, JobStatusEnum.IN_PROGRESS);
		final String jobId = "1";

		mockGreenEnergyAgent.manage().incrementFinishedJobs(jobId);
		assertThat(MOCK_MANAGEMENT.getUniqueFinishedJobs().get()).isZero();
		assertThat(MOCK_MANAGEMENT.getFinishedJobsInstances().get()).isEqualTo(1);
	}

	@Test
	@DisplayName("Test updating maximum capacity")
	void testUpdatingMaximumCapacity() {
		final int newCapacity = 1000;
		mockGreenEnergyAgent.manage().updateMaximumCapacity(newCapacity);

		assertThat(mockGreenEnergyAgent.getMaximumCapacity()).isEqualTo(1000);
		assertThat(mockGreenEnergyAgent.getInitialMaximumCapacity()).isEqualTo(MOCK_CAPACITY);
	}

	@Test
	@DisplayName("Test power job division - power job after shortage start")
	void testJobDivisionAfterShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final PowerJob powerJob = MOCK_POWER_JOBS.keySet().stream()
				.filter(jobKey -> jobKey.getJobId().equals("5")).findFirst()
				.orElse(null);

		mockGreenEnergyAgent.manage().dividePowerJobForPowerShortage(Objects.requireNonNull(powerJob), startTime);
		final JobStatusEnum statusAfterUpdate = mockGreenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().equals(powerJob))
				.map(Map.Entry::getValue)
				.findFirst().orElse(null);

		assertThat(mockGreenEnergyAgent.getPowerJobs()).hasSameSizeAs(MOCK_POWER_JOBS);
		assertTrue(mockGreenEnergyAgent.getPowerJobs().containsKey(powerJob));
		assertThat(statusAfterUpdate).isEqualTo(ON_HOLD_TRANSFER);
	}

	@Test
	@DisplayName("Test power job division - power job during shortage start")
	void testPowerJobDivisionDuringShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final PowerJob powerJob = MOCK_POWER_JOBS.keySet().stream()
				.filter(jobKey -> jobKey.getJobId().equals("2")).findFirst()
				.orElse(null);

		mockGreenEnergyAgent.manage().dividePowerJobForPowerShortage(Objects.requireNonNull(powerJob), startTime);

		final Map<PowerJob, JobStatusEnum> updatedJobInstances = mockGreenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().getJobId().equals(powerJob.getJobId()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Map.Entry<PowerJob, JobStatusEnum> jobOnHold = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> jobEntry.getValue().equals(ON_HOLD_TRANSFER))
				.findFirst().orElse(null);
		final Map.Entry<PowerJob, JobStatusEnum> jobInProgress = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> !jobEntry.getValue().equals(ON_HOLD_TRANSFER))
				.findFirst().orElse(null);

		assertThat(mockGreenEnergyAgent.getPowerJobs()).hasSize(6);
		assertFalse(mockGreenEnergyAgent.getPowerJobs().containsKey(powerJob));
		assertThat(updatedJobInstances).hasSize(2);
		assertNotNull(jobOnHold);
		assertNotNull(jobInProgress);
		assertThat(jobOnHold.getKey().getStartTime()).isEqualTo(startTime);
		assertThat(jobInProgress.getKey().getEndTime()).isEqualTo(startTime);
	}

	@Test
	@DisplayName("Test get jobs timetable with repeatable time instances")
	void testGetJobsTimetableRepeatableInstances() {
		final PowerJob mockCandidatePowerJob = ImmutablePowerJob.builder()
				.jobId("6")
				.power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.build();
		final List<Instant> result = mockGreenEnergyAgent.manage().getJobsTimetable(mockCandidatePowerJob);

		assertThat(result)
				.hasSize(10)
				.contains(Instant.parse("2022-01-01T13:00:00.000Z"))
				.contains(Instant.parse("2022-01-01T12:00:00.000Z"));
	}

	@Test
	@DisplayName("Test get jobs timetable with job in processing")
	void testGetJobsTimetableJobInProcessing() {
		final PowerJob mockCandidatePowerJob = ImmutablePowerJob.builder()
				.jobId("6")
				.power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.build();
		final PowerJob jobProcessing = ImmutablePowerJob.builder()
				.jobId("10")
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.power(10)
				.build();
		mockGreenEnergyAgent.getPowerJobs().put(jobProcessing, JobStatusEnum.PROCESSING);
		final List<Instant> result = mockGreenEnergyAgent.manage().getJobsTimetable(mockCandidatePowerJob);

		assertThat(result)
				.hasSize(10)
				.contains(Instant.parse("2022-01-01T13:00:00.000Z"))
				.contains(Instant.parse("2022-01-01T12:00:00.000Z"))
				.doesNotContain(Instant.parse("2022-01-01T13:30:00.000Z"));
	}

	@Test
	@DisplayName("Test get available capacity at given moment for positive power")
	void testGetAvailableCapacityAtGivenMoment() {
		doReturn(100.0).when(MOCK_POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(), any());
		final Instant mockMoment = Instant.parse("2022-01-01T09:00:00.000Z");
		final MonitoringData monitoringData = ImmutableMonitoringData.builder()
				.addWeatherData(MOCK_WEATHER)
				.build();
		final Optional<Double> result = mockGreenEnergyAgent.manage().getAvailablePower(mockMoment, monitoringData);

		assertThat(result).isPresent().contains(10.0);
	}

	@Test
	@DisplayName("Test get available capacity at given moment for negative power")
	void testGetAvailableCapacityAtGivenMomentNoPower() {
		doReturn(50.0).when(MOCK_POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(), any());
		final Instant mockMoment = Instant.parse("2022-01-01T09:00:00.000Z");
		final MonitoringData monitoringData = ImmutableMonitoringData.builder()
				.addWeatherData(MOCK_WEATHER)
				.build();
		final Optional<Double> result = mockGreenEnergyAgent.manage().getAvailablePower(mockMoment, monitoringData);

		assertThat(result).isEmpty();
	}

	// PREPARING TEST DATA

	/**
	 * Class creates mock green energy power jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * PowerJob1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
	 * PowerJob2 -> power: 20, time: 07:00 - 11:00, status: IN_PROGRESS
	 * PowerJob3 -> power: 50,  time: 06:00 - 15:00, status: ON_HOLD
	 * PowerJob4 -> power: 10,  time: 09:00 - 12:00, status: ON_HOLD
	 * PowerJob5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
	 */
	private Map<PowerJob, JobStatusEnum> setUpGreenEnergyJobs() {
		final PowerJob mockJob1 = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.power(10)
				.build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder()
				.jobId("2")
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.power(20)
				.build();
		final PowerJob mockJob3 = ImmutablePowerJob.builder()
				.jobId("3")
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.power(50)
				.build();
		final PowerJob mockJob4 = ImmutablePowerJob.builder()
				.jobId("4")
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.power(10)
				.build();
		final PowerJob mockJob5 = ImmutablePowerJob.builder()
				.jobId("5")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.power(25)
				.build();
		final Map<PowerJob, JobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob3, JobStatusEnum.ON_HOLD);
		mockJobMap.put(mockJob4, JobStatusEnum.ON_HOLD);
		mockJobMap.put(mockJob5, JobStatusEnum.ACCEPTED);
		return mockJobMap;
	}

	private void setUpGreenEnergyMock() {
		mockGreenEnergyAgent = spy(GreenEnergyAgent.class);
		mockGreenEnergyAgent.getPowerJobs().putAll(MOCK_POWER_JOBS);
		MOCK_POWER_MANAGEMENT = spy(new GreenPowerManagement(MOCK_CAPACITY, mockGreenEnergyAgent));
		final GreenEnergyStateManagement management = new GreenEnergyStateManagement(mockGreenEnergyAgent);
		MOCK_MANAGEMENT = spy(management);
		mockGreenEnergyAgent.setGreenPowerManagement(MOCK_POWER_MANAGEMENT);

		doReturn(MOCK_PRICE).when(mockGreenEnergyAgent).getPricePerPowerUnit();
		doReturn(MOCK_MANAGEMENT).when(mockGreenEnergyAgent).manage();
		doNothing().when(mockGreenEnergyAgent).addBehaviour(any());
		doNothing().when(mockGreenEnergyAgent).send(any());
	}
}
