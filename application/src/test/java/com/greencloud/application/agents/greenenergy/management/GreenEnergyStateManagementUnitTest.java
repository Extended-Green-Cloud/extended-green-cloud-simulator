package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.constants.CacheTestConstants.MOCK_WEATHER;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTimeMock;
import static com.greencloud.application.utils.TimeUtils.useMockTime;
import static com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum.WIND;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_TRANSFER;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.greencloud.application.agents.greenenergy.behaviour.adaptation.InitiateGreenSourceDisconnection;
import com.greencloud.application.agents.greenenergy.domain.GreenSourceDisconnection;
import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.ImmutableServerJob;
import com.greencloud.commons.job.JobResultType;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.commons.job.ServerJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class GreenEnergyStateManagementUnitTest {

	// MOCK OBJECTS

	private static final double MOCK_PRICE = 10;
	private static final int MOCK_CAPACITY = 100;
	private static Map<ServerJob, ExecutionJobStatusEnum> MOCK_POWER_JOBS;

	@Mock
	private static GreenEnergyAgent mockGreenEnergyAgent;
	@Mock
	private static GreenEnergyStateManagement MOCK_MANAGEMENT;
	@Mock
	private static GreenPowerManagement MOCK_POWER_MANAGEMENT;
	@Mock
	private static GreenEnergyAdaptationManagement MOCK_ADAPTATION_MANAGEMENT;

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
		final ServerJob serverJob = MOCK_POWER_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("5"))
				.findFirst().orElse(null);

		mockGreenEnergyAgent.manage().divideServerJobForPowerShortage(Objects.requireNonNull(serverJob), startTime);
		final ExecutionJobStatusEnum statusAfterUpdate = mockGreenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().equals(serverJob)).map(Map.Entry::getValue).findFirst()
				.orElse(null);

		assertThat(mockGreenEnergyAgent.getServerJobs()).hasSameSizeAs(MOCK_POWER_JOBS);
		assertTrue(mockGreenEnergyAgent.getServerJobs().containsKey(serverJob));
		assertThat(statusAfterUpdate).isEqualTo(ON_HOLD_TRANSFER);
	}

	@Test
	@DisplayName("Test power job division - power job during shortage start")
	void testPowerJobDivisionDuringShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final ServerJob serverJob = MOCK_POWER_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("2"))
				.findFirst().orElse(null);

		mockGreenEnergyAgent.manage().divideServerJobForPowerShortage(Objects.requireNonNull(serverJob), startTime);

		final Map<PowerJob, ExecutionJobStatusEnum> updatedJobInstances = mockGreenEnergyAgent.getServerJobs()
				.entrySet()
				.stream()
				.filter(jobEntry -> jobEntry.getKey().getJobId().equals(serverJob.getJobId()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Map.Entry<PowerJob, ExecutionJobStatusEnum> jobOnHold = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> jobEntry.getValue().equals(ON_HOLD_TRANSFER)).findFirst().orElse(null);
		final Map.Entry<PowerJob, ExecutionJobStatusEnum> jobInProgress = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> !jobEntry.getValue().equals(ON_HOLD_TRANSFER)).findFirst().orElse(null);

		assertThat(mockGreenEnergyAgent.getServerJobs()).hasSize(6);
		assertFalse(mockGreenEnergyAgent.getServerJobs().containsKey(serverJob));
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

		assertThat(result).isPresent().contains(70.0);
	}

	@Test
	@DisplayName("Test get available capacity at given moment for negative power")
	void testGetAvailableCapacityAtGivenMomentNoPower() {
		doReturn(10.0).when(MOCK_POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(), any());
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
		final ServerJob mockJob = ImmutableServerJob.builder().jobId("100")
				.server(mock(AID.class))
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
		final ServerJob mockJob = ImmutableServerJob.builder().jobId("100")
				.server(mock(AID.class))
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
		final ServerJob mockJob = ImmutableServerJob.builder()
				.server(mock(AID.class))
				.jobId("100")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T08:00:10.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20)
				.build();
		mockGreenEnergyAgent.setWeatherPredictionError(0.02);
		assertThat(mockGreenEnergyAgent.manage().computeCombinedPowerError(mockJob)).isEqualTo(0.03);
	}

	@Test
	@DisplayName("Test performing post job removal check for no disconnection undergoing")
	void testPerformPostJobRemovalCheckNoDisconnection() {
		clearInvocations(mockGreenEnergyAgent);
		MOCK_ADAPTATION_MANAGEMENT.setGreenSourceDisconnection(new GreenSourceDisconnection());

		mockGreenEnergyAgent.manage().performPostJobRemovalCheck();

		//
		verify(mockGreenEnergyAgent, times(0)).getServerJobs();
		verify(mockGreenEnergyAgent, times(0)).addBehaviour(any());
	}

	@Test
	@DisplayName("Test performing post job removal check if not all jobs cleared")
	void testPerformPostJobRemovalCheckNotAllJobsCleared() {
		var mockMessage = new ACLMessage(REQUEST);
		var mockServer = new AID("test_server", AID.ISGUID);

		var testDisconnection = new GreenSourceDisconnection(mockServer, mockMessage, true);
		MOCK_ADAPTATION_MANAGEMENT.setGreenSourceDisconnection(testDisconnection);

		clearInvocations(mockGreenEnergyAgent);
		clearInvocations(MOCK_ADAPTATION_MANAGEMENT);

		mockGreenEnergyAgent.manage().performPostJobRemovalCheck();

		verify(MOCK_ADAPTATION_MANAGEMENT, times(2)).getGreenSourceDisconnectionState();
		verify(mockGreenEnergyAgent, times(1)).getServerJobs();
		verify(mockGreenEnergyAgent, times(0)).addBehaviour(any());
	}

	@Test
	@DisplayName("Test performing post job removal check if all jobs cleared")
	void testPerformPostJobRemovalCheckAllJobsCleared() {
		var mockMessage = new ACLMessage(REQUEST);
		var mockServer = new AID("test_server1", AID.ISGUID);

		var testDisconnection = new GreenSourceDisconnection(mockServer, mockMessage, true);
		MOCK_ADAPTATION_MANAGEMENT.setGreenSourceDisconnection(testDisconnection);

		clearInvocations(mockGreenEnergyAgent);
		clearInvocations(MOCK_ADAPTATION_MANAGEMENT);

		mockGreenEnergyAgent.manage().performPostJobRemovalCheck();

		verify(MOCK_ADAPTATION_MANAGEMENT, times(4)).getGreenSourceDisconnectionState();
		verify(mockGreenEnergyAgent, times(1)).getServerJobs();
		verify(mockGreenEnergyAgent, times(1)).addBehaviour(
				argThat(behaviour -> behaviour instanceof InitiateGreenSourceDisconnection));
	}

	@Test
	@DisplayName("Test job removal")
	void testRemoveJob() {
		var mockMessage = new ACLMessage(REQUEST);
		var mockServer = new AID("test_server1", AID.ISGUID);
		var testDisconnection = new GreenSourceDisconnection(mockServer, mockMessage, true);
		MOCK_ADAPTATION_MANAGEMENT.setGreenSourceDisconnection(testDisconnection);

		var testJob = ImmutableServerJob.builder()
				.jobId("100")
				.server(new AID("test_server1", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();
		mockGreenEnergyAgent.getServerJobs().put(testJob, IN_PROGRESS);

		clearInvocations(mockGreenEnergyAgent);
		clearInvocations(MOCK_ADAPTATION_MANAGEMENT);

		mockGreenEnergyAgent.manage().removeJob(testJob);

		verify(MOCK_ADAPTATION_MANAGEMENT, times(4)).getGreenSourceDisconnectionState();
		verify(mockGreenEnergyAgent, times(2)).getServerJobs();
		verify(mockGreenEnergyAgent, times(1)).addBehaviour(
				argThat(behaviour -> behaviour instanceof InitiateGreenSourceDisconnection));

		assertThat(mockGreenEnergyAgent.getServerJobs()).doesNotContainKey(testJob);
	}

	// PREPARING TEST DATA

	/**
	 * Class creates mock green energy server jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * ServerJob1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
	 * ServerJob2 -> power: 20, time: 07:00 - 11:00, status: IN_PROGRESS
	 * ServerJob3 -> power: 50,  time: 06:00 - 15:00, status: ON_HOLD
	 * ServerJob4 -> power: 10,  time: 09:00 - 12:00, status: ON_HOLD
	 * ServerJob5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
	 */
	private Map<ServerJob, ExecutionJobStatusEnum> setUpGreenEnergyJobs() {
		final ServerJob mockJob1 = ImmutableServerJob.builder().jobId("1")
				.server(new AID("test_server", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ServerJob mockJob2 = ImmutableServerJob.builder().jobId("2")
				.server(new AID("test_server", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final ServerJob mockJob3 = ImmutableServerJob.builder().jobId("3")
				.server(new AID("test_server", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(50).build();
		final ServerJob mockJob4 = ImmutableServerJob.builder().jobId("4")
				.server(new AID("test_server", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ServerJob mockJob5 = ImmutableServerJob.builder().jobId("5")
				.server(new AID("test_server", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(25).build();
		final Map<ServerJob, ExecutionJobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob3, ExecutionJobStatusEnum.ON_HOLD_PLANNED);
		mockJobMap.put(mockJob4, ExecutionJobStatusEnum.ON_HOLD_PLANNED);
		mockJobMap.put(mockJob5, ExecutionJobStatusEnum.ACCEPTED);
		return mockJobMap;
	}

	private void setUpGreenEnergyMock() {
		mockGreenEnergyAgent = spy(GreenEnergyAgent.class);
		mockGreenEnergyAgent.getServerJobs().putAll(MOCK_POWER_JOBS);
		MOCK_POWER_MANAGEMENT = spy(new GreenPowerManagement(MOCK_CAPACITY, mockGreenEnergyAgent));
		final GreenEnergyStateManagement management = new GreenEnergyStateManagement(mockGreenEnergyAgent);
		MOCK_MANAGEMENT = spy(management);
		MOCK_ADAPTATION_MANAGEMENT = spy(new GreenEnergyAdaptationManagement(mockGreenEnergyAgent));
		mockGreenEnergyAgent.setGreenPowerManagement(MOCK_POWER_MANAGEMENT);

		doReturn(WIND).when(mockGreenEnergyAgent).getEnergyType();
		doReturn(MOCK_PRICE).when(mockGreenEnergyAgent).getPricePerPowerUnit();
		doReturn(MOCK_MANAGEMENT).when(mockGreenEnergyAgent).manage();
		doReturn(MOCK_ADAPTATION_MANAGEMENT).when(mockGreenEnergyAgent).adapt();
		doNothing().when(mockGreenEnergyAgent).addBehaviour(any());
		doNothing().when(mockGreenEnergyAgent).send(any());
	}
}
