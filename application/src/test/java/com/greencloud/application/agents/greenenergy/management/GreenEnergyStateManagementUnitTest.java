package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum.WIND;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_WEATHER;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_TRANSFER;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTimeMock;
import static com.greencloud.application.utils.TimeUtils.useMockTime;
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
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.greencloud.commons.job.ExecutionJobStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.job.ImmutablePowerJob;
import com.greencloud.commons.job.JobResultType;
import com.greencloud.commons.job.PowerJob;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class GreenEnergyStateManagementUnitTest {

	// MOCK OBJECTS

	private static final double MOCK_PRICE = 10;
	private static final int MOCK_CAPACITY = 100;
	private static Map<PowerJob, ExecutionJobStatusEnum> MOCK_POWER_JOBS;

	@Mock
	private static GreenEnergyAgent mockGreenEnergyAgent;
	@Mock
	private static GreenEnergyStateManagement MOCK_MANAGEMENT;
	@Mock
	private static GreenPowerManagement MOCK_POWER_MANAGEMENT;

	// TEST SET-UP

	@BeforeAll
	static void setUpAll() {
		useMockTime(Instant.parse("2022-01-01T09:00:00.000Z"), ZoneId.of("UTC"));
		setSystemStartTimeMock(Instant.parse("2022-01-01T05:00:00.000Z"));
	}

	@BeforeEach
	void init() {
		MOCK_POWER_JOBS = setUpGreenEnergyJobs();
		setUpGreenEnergyMock();
	}

	// TESTS

	@ParameterizedTest
	@EnumSource(JobResultType.class)
	@DisplayName("Test increment started unique job")
	void testIncrementCounter(JobResultType type) {
		final JobInstanceIdentifier jobInstanceId = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();

		mockGreenEnergyAgent.manage().incrementJobCounter(jobInstanceId, type);
		assertThat(MOCK_MANAGEMENT.getJobCounters()).containsEntry(type, 1L);
	}

	@Test
	@DisplayName("Test updating maximum capacity")
	void testUpdatingMaximumCapacity() {
		final int newCapacity = 1000;
		mockGreenEnergyAgent.manage().updateMaximumCapacity(newCapacity);

		assertThat(mockGreenEnergyAgent.manageGreenPower().getCurrentMaximumCapacity()).isEqualTo(1000);
		assertThat(mockGreenEnergyAgent.manageGreenPower().getInitialMaximumCapacity()).isEqualTo(MOCK_CAPACITY);
	}

	@Test
	@DisplayName("Test power job division - power job after shortage start")
	void testJobDivisionAfterShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final PowerJob powerJob = MOCK_POWER_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("5"))
				.findFirst().orElse(null);

		mockGreenEnergyAgent.manage().dividePowerJobForPowerShortage(Objects.requireNonNull(powerJob), startTime);
		final ExecutionJobStatusEnum statusAfterUpdate = mockGreenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().equals(powerJob)).map(Map.Entry::getValue).findFirst()
				.orElse(null);

		assertThat(mockGreenEnergyAgent.getPowerJobs()).hasSameSizeAs(MOCK_POWER_JOBS);
		assertTrue(mockGreenEnergyAgent.getPowerJobs().containsKey(powerJob));
		assertThat(statusAfterUpdate).isEqualTo(ON_HOLD_TRANSFER);
	}

	@Test
	@DisplayName("Test power job division - power job during shortage start")
	void testPowerJobDivisionDuringShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final PowerJob powerJob = MOCK_POWER_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("2"))
				.findFirst().orElse(null);

		mockGreenEnergyAgent.manage().dividePowerJobForPowerShortage(Objects.requireNonNull(powerJob), startTime);

		final Map<PowerJob, ExecutionJobStatusEnum> updatedJobInstances = mockGreenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().getJobId().equals(powerJob.getJobId()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Map.Entry<PowerJob, ExecutionJobStatusEnum> jobOnHold = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> jobEntry.getValue().equals(ON_HOLD_TRANSFER)).findFirst().orElse(null);
		final Map.Entry<PowerJob, ExecutionJobStatusEnum> jobInProgress = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> !jobEntry.getValue().equals(ON_HOLD_TRANSFER)).findFirst().orElse(null);

		assertThat(mockGreenEnergyAgent.getPowerJobs()).hasSize(6);
		assertFalse(mockGreenEnergyAgent.getPowerJobs().containsKey(powerJob));
		assertThat(updatedJobInstances).hasSize(2);
		assertNotNull(jobOnHold);
		assertNotNull(jobInProgress);
		assertThat(jobOnHold.getKey().getStartTime()).isEqualTo(startTime);
		assertThat(jobInProgress.getKey().getEndTime()).isEqualTo(startTime);
	}

	@Test
	@DisplayName("Test get available capacity at given moment for positive power")
	void testGetAvailableCapacityAtGivenMoment() {
		doReturn(100.0).when(MOCK_POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(), any());
		final Instant mockMoment = convertToRealTime(Instant.parse("2022-01-01T09:00:00.000Z"));
		final MonitoringData monitoringData = ImmutableMonitoringData.builder().addWeatherData(MOCK_WEATHER).build();
		final Optional<Double> result = mockGreenEnergyAgent.manage().getAvailablePower(mockMoment, monitoringData);

		assertThat(result).isPresent().contains(10.0);
	}

	@Test
	@DisplayName("Test get available capacity at given moment for negative power")
	void testGetAvailableCapacityAtGivenMomentNoPower() {
		doReturn(50.0).when(MOCK_POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(), any());
		final Instant mockMoment = convertToRealTime(Instant.parse("2022-01-01T09:00:00.000Z"));
		final MonitoringData monitoringData = ImmutableMonitoringData.builder().addWeatherData(MOCK_WEATHER).build();
		final Optional<Double> result = mockGreenEnergyAgent.manage().getAvailablePower(mockMoment, monitoringData);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Test get current power in use")
	void testGetCurrentPowerInUse() {
		assertThat(mockGreenEnergyAgent.manage().getCurrentPowerInUseForGreenSource()).isEqualTo(30);
	}

	@Test
	@DisplayName("Test get available power for job when job is new")
	void testGetAvailablePowerForNewJob() {
		final PowerJob mockJob = ImmutablePowerJob.builder().jobId("100")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final MonitoringData monitoringData = ImmutableMonitoringData.builder().addWeatherData(MOCK_WEATHER).build();
		final Optional<Double> result = mockGreenEnergyAgent.manage()
				.getAvailablePowerForJob(mockJob, monitoringData, true);

		assertThat(result).contains(10.0);
	}

	@Test
	@DisplayName("Test get available power for job when job is not new")
	void testGetAvailablePowerForNotNewJob() {
		final PowerJob mockJob = ImmutablePowerJob.builder().jobId("100")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final MonitoringData monitoringData = ImmutableMonitoringData.builder().addWeatherData(MOCK_WEATHER).build();
		final Optional<Double> result = mockGreenEnergyAgent.manage()
				.getAvailablePowerForJob(mockJob, monitoringData, false);

		assertThat(result).contains(70.0);
	}

	@Test
	@DisplayName("Test computing overall error for job")
	void testComputeCombinedPowerError() {
		final PowerJob mockJob = ImmutablePowerJob.builder()
				.jobId("100")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T08:00:10.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20)
				.build();
		assertThat(mockGreenEnergyAgent.manage().computeCombinedPowerError(mockJob)).isEqualTo(0.03);
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
	private Map<PowerJob, ExecutionJobStatusEnum> setUpGreenEnergyJobs() {
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
		final PowerJob mockJob3 = ImmutablePowerJob.builder().jobId("3")
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(50).build();
		final PowerJob mockJob4 = ImmutablePowerJob.builder().jobId("4")
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final PowerJob mockJob5 = ImmutablePowerJob.builder().jobId("5")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(25).build();
		final Map<PowerJob, ExecutionJobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob3, ExecutionJobStatusEnum.ON_HOLD_PLANNED);
		mockJobMap.put(mockJob4, ExecutionJobStatusEnum.ON_HOLD_PLANNED);
		mockJobMap.put(mockJob5, ExecutionJobStatusEnum.ACCEPTED);
		return mockJobMap;
	}

	private void setUpGreenEnergyMock() {
		mockGreenEnergyAgent = spy(GreenEnergyAgent.class);
		mockGreenEnergyAgent.getPowerJobs().putAll(MOCK_POWER_JOBS);
		MOCK_POWER_MANAGEMENT = spy(new GreenPowerManagement(MOCK_CAPACITY, mockGreenEnergyAgent));
		final GreenEnergyStateManagement management = new GreenEnergyStateManagement(mockGreenEnergyAgent);
		MOCK_MANAGEMENT = spy(management);
		mockGreenEnergyAgent.setGreenPowerManagement(MOCK_POWER_MANAGEMENT);

		doReturn(WIND).when(mockGreenEnergyAgent).getEnergyType();
		doReturn(MOCK_PRICE).when(mockGreenEnergyAgent).getPricePerPowerUnit();
		doReturn(MOCK_MANAGEMENT).when(mockGreenEnergyAgent).manage();
		doNothing().when(mockGreenEnergyAgent).addBehaviour(any());
		doNothing().when(mockGreenEnergyAgent).send(any());
	}
}
