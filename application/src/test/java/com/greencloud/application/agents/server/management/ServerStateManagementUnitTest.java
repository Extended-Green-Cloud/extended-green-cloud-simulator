package com.greencloud.application.agents.server.management;

import static com.greencloud.commons.job.ExecutionJobStatusEnum.BACK_UP_POWER_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.GREEN_ENERGY_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_TRANSFER;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTimeMock;
import static com.greencloud.commons.job.JobResultType.FINISH;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.utils.TimeUtils;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;
import com.greencloud.commons.job.JobResultType;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ServerStateManagementUnitTest {

	// MOCKED OBJECTS
	private static final Instant MOCK_NOW = parse("2022-01-01T11:00:00.000Z");
	private static final int MOCK_CAPACITY = 200;
	private static final double MOCK_PRICE = 10;
	private static ServerStateManagement MOCK_MANAGEMENT;
	private static Map<ClientJob, ExecutionJobStatusEnum> MOCK_JOBS;

	@Mock
	private static ServerAgent serverAgent;

	// TEST SET-UP

	@BeforeAll
	static void setUpAll() {
		TimeUtils.useMockTime(MOCK_NOW, ZoneId.of("UTC"));
		setSystemStartTimeMock(MOCK_NOW);
	}

	@BeforeEach
	void init() {
		MOCK_JOBS = setUpServerJobs();
		setUpServerMock();
	}

	// TESTS

	@Test
	@DisplayName("Test getting available capacity for ALL jobs")
	void testGettingAvailableCapacityForAllJobs() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final Instant endTime = Instant.parse("2022-01-01T12:30:00.000Z");
		final int availableCapacity = serverAgent.manage().getAvailableCapacity(startTime, endTime, null, null);

		assertThat(availableCapacity).isEqualTo(153);
	}

	@Test
	@DisplayName("Test getting available capacity for ALL jobs with excluded job")
	void testGettingAvailableCapacityForAllJobsWithOneExcluded() {
		final JobInstanceIdentifier jobToExclude = ImmutableJobInstanceIdentifier.builder()
				.jobId("5")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final Instant endTime = Instant.parse("2022-01-01T12:30:00.000Z");
		final int availableCapacity = serverAgent.manage().getAvailableCapacity(startTime, endTime, jobToExclude, null);

		assertThat(availableCapacity).isEqualTo(171);
	}

	@Test
	@DisplayName("Test getting available capacity for BACK UP jobs")
	void testGettingAvailableCapacityForBackUpJobs() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final Instant endTime = Instant.parse("2022-01-01T12:30:00.000Z");
		final int availableCapacity = serverAgent.manage()
				.getAvailableCapacity(startTime, endTime, null, BACK_UP_POWER_STATUSES);

		assertThat(availableCapacity).isEqualTo(188);
	}

	@Test
	@DisplayName("Test getting available capacity for GREEN ENERGY jobs")
	void testGettingAvailableCapacityForGreenEnergyJobs() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final Instant endTime = Instant.parse("2022-01-01T12:30:00.000Z");
		final int availableCapacity = serverAgent.manage()
				.getAvailableCapacity(startTime, endTime, null, GREEN_ENERGY_STATUSES);

		assertThat(availableCapacity).isEqualTo(173);
	}

	@Test
	@DisplayName("Test getting available capacity for ALL jobs with PROCESSING")
	void testGettingAvailableCapacityForAllJobsIncludingProcessing() {
		final ClientJob jobProcessing = ImmutableClientJob.builder()
				.jobId("1000")
				.clientIdentifier("Client1000")
				.startTime(Instant.parse("2022-01-01T05:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();
		serverAgent.getServerJobs().put(jobProcessing, ExecutionJobStatusEnum.PROCESSING);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final Instant endTime = Instant.parse("2022-01-01T12:30:00.000Z");
		final int availableCapacity = serverAgent.manage().getAvailableCapacity(startTime, endTime, null, null);

		assertThat(availableCapacity).isEqualTo(153);
	}

	@ParameterizedTest
	@EnumSource(JobResultType.class)
	@DisplayName("Test increment started unique job")
	void testIncrementCounter(JobResultType type) {
		final JobInstanceIdentifier jobInstanceId = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();

		serverAgent.manage().incrementJobCounter(jobInstanceId, type);
		assertThat(MOCK_MANAGEMENT.getJobCounters()).containsEntry(type, 1L);
	}

	@Test
	@DisplayName("Test update maximum capacity")
	void testUpdateMaximumCapacity() {
		final int newCapacity = 50;

		serverAgent.manage().updateMaximumCapacity(newCapacity);
		assertThat(serverAgent.getCurrentMaximumCapacity()).isEqualTo(newCapacity);
	}

	@Test
	@DisplayName("Test job division - job after shortage start")
	void testJobDivisionAfterShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final ClientJob job = MOCK_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("5")).findFirst()
				.orElse(null);

		serverAgent.manage().divideJobForPowerShortage(Objects.requireNonNull(job), startTime);
		final ExecutionJobStatusEnum statusAfterUpdate = serverAgent.getServerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().equals(job))
				.map(Map.Entry::getValue)
				.findFirst().orElse(null);

		assertThat(serverAgent.getServerJobs()).hasSameSizeAs(MOCK_JOBS);
		assertTrue(serverAgent.getServerJobs().containsKey(job));
		assertThat(statusAfterUpdate).isEqualTo(ON_HOLD_TRANSFER);
	}

	@Test
	@DisplayName("Test job division - job during shortage start")
	void testJobDivisionDuringShortageStart() {
		final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
		final ClientJob job = MOCK_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("2")).findFirst()
				.orElse(null);

		serverAgent.manage().divideJobForPowerShortage(Objects.requireNonNull(job), startTime);

		final Map<ClientJob, ExecutionJobStatusEnum> updatedJobInstances = serverAgent.getServerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().getJobId().equals(job.getJobId()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Map.Entry<ClientJob, ExecutionJobStatusEnum> jobOnHold = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> jobEntry.getValue().equals(ON_HOLD_TRANSFER))
				.findFirst().orElse(null);
		final Map.Entry<ClientJob, ExecutionJobStatusEnum> jobInProgress = updatedJobInstances.entrySet().stream()
				.filter(jobEntry -> !jobEntry.getValue().equals(ON_HOLD_TRANSFER))
				.findFirst().orElse(null);

		assertThat(serverAgent.getServerJobs()).hasSize(7);
		assertFalse(serverAgent.getServerJobs().containsKey(job));
		assertThat(updatedJobInstances).hasSize(2);
		assertNotNull(jobOnHold);
		assertNotNull(jobInProgress);
		assertThat(jobOnHold.getKey().getStartTime()).isEqualTo(startTime);
		assertThat(jobInProgress.getKey().getEndTime()).isEqualTo(startTime);
	}

	@Test
	@DisplayName("Test finishing job not on back up power")
	void testFinishingJobNotBackUpPower() {
		final AID greenSourceForJob = mock(AID.class);
		final ClientJob job = MOCK_JOBS.keySet().stream()
				.filter(jobKey -> jobKey.getJobId().equals("1")).findFirst()
				.orElse(null);

		serverAgent.getGreenSourceForJobMap().put(Objects.requireNonNull(job).getJobId(), greenSourceForJob);
		assertThat(serverAgent.getGreenSourceForJobMap()).hasSize(1);

		serverAgent.manage().finishJobExecution(job, false);

		assertThat(serverAgent.getServerJobs()).hasSize(5);
		assertThat(serverAgent.getGreenSourceForJobMap()).isEmpty();
		assertThat(MOCK_MANAGEMENT.getJobCounters()).containsEntry(FINISH, 1L);
	}

	@Test
	@DisplayName("Test finishing job on back up power")
	void testFinishingJobBackUpPower() {
		final AID greenSourceForJob = mock(AID.class);
		final ClientJob job = MOCK_JOBS.keySet().stream().filter(jobKey -> jobKey.getJobId().equals("2")).findFirst()
				.orElse(null);
		serverAgent.getGreenSourceForJobMap().put(Objects.requireNonNull(job).getJobId(), greenSourceForJob);

		serverAgent.manage().finishJobExecution(job, false);

		final ExecutionJobStatusEnum updatedStatus = serverAgent.getServerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().getJobId().equals("3"))
				.map(Map.Entry::getValue)
				.findFirst().orElse(null);

		assertThat(serverAgent.getServerJobs()).hasSize(5);
		assertThat(MOCK_MANAGEMENT.getJobCounters()).containsEntry(FINISH, 1L);
		assertNotNull(updatedStatus);
		assertThat(updatedStatus).isEqualTo(IN_PROGRESS_BACKUP_ENERGY_PLANNED);
	}

	@Test
	@DisplayName("Test getting active owned Green Sources")
	void testGetOwnedActiveGreenSources() {
		var testGreenSources = Map.of(
				new AID("test_gs1", AID.ISGUID), true,
				new AID("test_gs2", AID.ISGUID), false,
				new AID("test_gs3", AID.ISGUID), true
		);

		serverAgent.getOwnedGreenSources().clear();
		serverAgent.getOwnedGreenSources().putAll(testGreenSources);

		var result = serverAgent.manage().getOwnedActiveGreenSources();

		assertThat(result)
				.hasSize(2)
				.allMatch(greenSource -> Set.of("test_gs1", "test_gs3").contains(greenSource.getName()));
	}

	/**
	 * Class creates mock server jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * Job1 -> power: 10, time: 08:00 - 10:30, status: IN_PROGRESS,
	 * Job2 -> power: 12, time: 07:30 - 11:00, status: IN_PROGRESS_BACKUP
	 * Job3 -> power: 5,  time: 06:00 - 15:00, status: ON_HOLD_SOURCE_SHORTAGE
	 * Job4 -> power: 2,  time: 09:00 - 12:00, status: IN_PROGRESS
	 * Job5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
	 * Job6 -> power: 15, time: 11:30 - 13:00, status: ON_HOLD_TRANSFER
	 */
	private Map<ClientJob, ExecutionJobStatusEnum> setUpServerJobs() {
		final ClientJob mockJob1 = ImmutableClientJob.builder()
				.jobId("1")
				.clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();
		final ClientJob mockJob2 = ImmutableClientJob.builder()
				.jobId("2")
				.clientIdentifier("Client2")
				.startTime(Instant.parse("2022-01-01T07:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(12)
				.build();
		final ClientJob mockJob3 = ImmutableClientJob.builder()
				.jobId("3")
				.clientIdentifier("Client3")
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5)
				.build();
		final ClientJob mockJob4 = ImmutableClientJob.builder()
				.jobId("4")
				.clientIdentifier("Client4")
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(2)
				.build();
		final ClientJob mockJob5 = ImmutableClientJob.builder()
				.jobId("5")
				.clientIdentifier("Client5")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(25)
				.build();
		final ClientJob mockJob6 = ImmutableClientJob.builder()
				.jobId("6")
				.clientIdentifier("Client6")
				.startTime(Instant.parse("2022-01-01T11:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15)
				.build();
		final Map<ClientJob, ExecutionJobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, ExecutionJobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED);
		mockJobMap.put(mockJob3, ExecutionJobStatusEnum.ON_HOLD_SOURCE_SHORTAGE_PLANNED);
		mockJobMap.put(mockJob4, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob5, ExecutionJobStatusEnum.ACCEPTED);
		mockJobMap.put(mockJob6, ON_HOLD_TRANSFER);
		return mockJobMap;
	}

	private void setUpServerMock() {
		serverAgent = spy(ServerAgent.class);
		serverAgent.getServerJobs().putAll(MOCK_JOBS);
		serverAgent.setCurrentMaximumCapacity(MOCK_CAPACITY);

		final ServerConfigManagement configManagement = spy(new ServerConfigManagement(serverAgent));
		MOCK_MANAGEMENT = spy(new ServerStateManagement(serverAgent));
		serverAgent.setAgentNode(null);

		doReturn(MOCK_PRICE).when(configManagement).getPricePerHour();
		doReturn(MOCK_CAPACITY).when(serverAgent).getInitialMaximumCapacity();

		doReturn(MOCK_MANAGEMENT).when(serverAgent).manage();
		doReturn(configManagement).when(serverAgent).manageConfig();

		doNothing().when(serverAgent).addBehaviour(any());
		doNothing().when(serverAgent).send(any());
	}
}
