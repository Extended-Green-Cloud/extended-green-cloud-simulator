package com.greencloud.application.utils;

import static com.greencloud.application.utils.AlgorithmUtils.computeIncorrectMaximumValProbability;
import static com.greencloud.application.utils.AlgorithmUtils.findJobsWithinPower;
import static com.greencloud.application.utils.AlgorithmUtils.getMaximumUsedPowerDuringTimeStamp;
import static com.greencloud.application.utils.AlgorithmUtils.getMinimalAvailablePowerDuringTimeStamp;
import static com.greencloud.application.utils.AlgorithmUtils.nextFibonacci;
import static com.greencloud.application.utils.AlgorithmUtils.previousFibonacci;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.ImmutableWeatherData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;
import com.greencloud.commons.job.ImmutablePowerJob;
import com.greencloud.commons.job.PowerJob;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class AlgorithmUtilsUnitTest {

	// MOCK OBJECTS
	private static final long MOCK_INTERVALS_LENGTH = 2L;
	private static final int MOCK_CAPACITY = 100;
	private static final Instant MOCK_TIME = Instant.parse("2022-01-01T10:00:00Z");
	private static MonitoringData MOCK_MONITORING_DATA;
	@Mock
	private GreenPowerManagement POWER_MANAGEMENT;

	// GETTING MAXIMUM POWER WITHIN TIMESTAMP TESTS

	@Test
	@DisplayName("Test maximum capacity calculation for no jobs")
	void testMaximumCapacityEmptyJobs() {
		final Set<ClientJob> jobList = Collections.EMPTY_SET;
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 1 Job")
	void testMaximumCapacityForOneJob() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final Set<ClientJob> jobList = Set.of(mockJob1);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(10);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs overlapping in time interval")
	void testMaximumCapacityForTwoOverlappingJobs() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T10:30:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();

		final Set<ClientJob> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs starting before time interval")
	void testMaximumCapacityForJobStartingBeforeTimeInterval() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T07:30:00Z"))
				.endTime(Instant.parse("2022-01-01T10:15:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();

		final Set<ClientJob> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs finishing after time interval")
	void testMaximumCapacityForJobFinishingAfterTimeInterval() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T10:30:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T10:45:00Z"))
				.endTime(Instant.parse("2022-01-01T12:15:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();

		final Set<ClientJob> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs overlapping time interval")
	void testMaximumCapacityForJobOverlappingTimeInterval() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T09:15:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();

		final Set<ClientJob> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 3 Jobs with 1 outside interval")
	void testMaximumCapacityForJobOutsideInterval() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:10:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T10:15:00Z"))
				.endTime(Instant.parse("2022-01-01T11:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final ClientJob mockJob3 = ImmutableClientJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T07:15:00Z"))
				.endTime(Instant.parse("2022-01-01T08:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();

		final Set<ClientJob> jobList = Set.of(mockJob1, mockJob2, mockJob3);
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(30);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 non-overlapping Jobs")
	void testMaximumCapacityForNoOverlappingJobs() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:15:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();

		final Set<ClientJob> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:30:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(15);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for complicated scenario")
	void testMaximumCapacityForComplicatedScenario() {
		final Set<ClientJob> jobList = jobsForFirstComplicatedScenario();
		final Instant startTime = Instant.parse("2022-01-01T08:45:00Z");
		final Instant endTime = Instant.parse("2022-01-01T12:45:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(15);
	}

	// GETTING JOBS WITHIN POWER TESTS

	private Set<ClientJob> jobsForFirstComplicatedScenario() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(1).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:00:00Z"))
				.endTime(Instant.parse("2022-01-01T09:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(2).build();
		final ClientJob mockJob3 = ImmutableClientJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T08:40:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(3).build();
		final ClientJob mockJob4 = ImmutableClientJob.builder().jobId("4").clientIdentifier("Test Client 4")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(4).build();
		final ClientJob mockJob5 = ImmutableClientJob.builder().jobId("5").clientIdentifier("Test Client 5")
				.startTime(Instant.parse("2022-01-01T10:45:00Z"))
				.endTime(Instant.parse("2022-01-01T11:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5).build();
		final ClientJob mockJob6 = ImmutableClientJob.builder().jobId("6").clientIdentifier("Test Client 6")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(6).build();
		final ClientJob mockJob7 = ImmutableClientJob.builder().jobId("7").clientIdentifier("Test Client 7")
				.startTime(Instant.parse("2022-01-01T12:15:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		return Set.of(mockJob1, mockJob2, mockJob3, mockJob4, mockJob5, mockJob6, mockJob7);
	}

	@Test
	@DisplayName("Jobs within power for empty list")
	void testJobsWithinPowerForEmptyList() {
		final int maxPower = 10;

		final List<ClientJob> result = findJobsWithinPower(Collections.emptyList(), maxPower);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Jobs within power for power equal zero")
	void testJobsWithinPowerForZeroPower() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();
		final List<ClientJob> mockJobs = List.of(mockJob1);
		final int maxPower = 0;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Jobs within power for 1 job")
	void testJobsWithinPowerForOneJob() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5).build();
		final List<ClientJob> mockJobs = List.of(mockJob1);
		final int maxPower = 10;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Jobs within power for 1 job with too much power")
	void testJobsWithinPowerForOneJobTooMuchPower() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();
		final List<ClientJob> mockJobs = List.of(mockJob1);
		final int maxPower = 10;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Jobs within power for two jobs with too much power")
	void testJobsWithinPowerForTwoJobsTooMuchPower() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();
		final List<ClientJob> mockJobs = List.of(mockJob1, mockJob2);
		final int maxPower = 10;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Jobs within power for two jobs with one greater power")
	void testJobsWithinPowerForTwoJobsOneGreaterPower() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(8).build();
		final List<ClientJob> mockJobs = List.of(mockJob1, mockJob2);
		final int maxPower = 10;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJobId()).isEqualTo("2");
	}

	@Test
	@DisplayName("Jobs within power for two jobs both included")
	void testJobsWithinPowerForTwoJobsBothInsidePower() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(8).build();
		final List<ClientJob> mockJobs = List.of(mockJob1, mockJob2);
		final int maxPower = 15;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(2);
		assertThat(result.get(1).getJobId()).isEqualTo("2");
	}

	@Test
	@DisplayName("Jobs within power for 3 jobs all within power")
	void testJobsWithinPowerForThreeJobsWithinPower() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		final ClientJob mockJob3 = ImmutableClientJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(4).build();
		final List<ClientJob> mockJobs = List.of(mockJob1, mockJob2, mockJob3);
		final int maxPower = 15;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(2);
		assertTrue(result.containsAll(List.of(mockJob1, mockJob2)));
	}

	@Test
	@DisplayName("Jobs within power for complicated scenario")
	void testJobsWithinPowerForComplicatedScenario() {
		final List<ClientJob> mockJobs = jobsForComplicatedPowerScenario();
		final int maxPower = 25;

		final List<ClientJob> result = findJobsWithinPower(mockJobs, maxPower);
		final int resultSum = result.stream().mapToInt(ClientJob::getPower).sum();

		assertFalse(result.isEmpty());
		assertThat(resultSum).isEqualTo(25);
	}

	@Test
	@DisplayName("Jobs within power for power job")
	void testJobsWithinPowerForPowerJob() {
		final PowerJob mockJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5).build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		final PowerJob mockJob3 = ImmutablePowerJob.builder().jobId("3")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(4).build();
		final List<PowerJob> mockJobs = List.of(mockJob1, mockJob2, mockJob3);
		final int maxPower = 10;

		final List<PowerJob> result = findJobsWithinPower(mockJobs, maxPower);
		final int resultSum = result.stream().mapToInt(PowerJob::getPower).sum();

		assertFalse(result.isEmpty());
		assertThat(resultSum).isEqualTo(9);
		assertThat(result).hasSize(2);
		assertTrue(result.containsAll(List.of(mockJob1, mockJob3)));
	}

	// GETTING MINIMAL AVAILABLE POWER TESTS

	private List<ClientJob> jobsForComplicatedPowerScenario() {
		final ClientJob mockJob1 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5).build();
		final ClientJob mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:00:00Z"))
				.endTime(Instant.parse("2022-01-01T09:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ClientJob mockJob3 = ImmutableClientJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T08:40:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(3).build();
		final ClientJob mockJob4 = ImmutableClientJob.builder().jobId("4").clientIdentifier("Test Client 4")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(2).build();
		final ClientJob mockJob5 = ImmutableClientJob.builder().jobId("5").clientIdentifier("Test Client 5")
				.startTime(Instant.parse("2022-01-01T10:45:00Z"))
				.endTime(Instant.parse("2022-01-01T11:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(16).build();
		final ClientJob mockJob6 = ImmutableClientJob.builder().jobId("6").clientIdentifier("Test Client 6")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(26).build();
		final ClientJob mockJob7 = ImmutableClientJob.builder().jobId("7").clientIdentifier("Test Client 7")
				.startTime(Instant.parse("2022-01-01T12:15:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(7).build();
		final ClientJob mockJob8 = ImmutableClientJob.builder().jobId("8").clientIdentifier("Test Client 8")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(8).build();
		final ClientJob mockJob9 = ImmutableClientJob.builder().jobId("9").clientIdentifier("Test Client 9")
				.startTime(Instant.parse("2022-01-01T12:15:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(14).build();
		return List.of(mockJob1, mockJob2, mockJob3, mockJob4, mockJob5, mockJob6, mockJob7, mockJob8, mockJob9);
	}

	@Test
	@DisplayName("Test minimal available power for no jobs")
	void testMinimalAvailablePowerEmptyJobs() {
		prepareMockManagement();
		final Set<ClientJob> jobList = Collections.EMPTY_SET;
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobList, startTime, endTime,
				MOCK_INTERVALS_LENGTH, POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(100);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 1 Job")
	void testMinimalAvailablePowerForOneJob() {
		prepareMockManagement();
		final PowerJob mockPowerJob = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(90);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 2 overlapping jobs")
	void testMinimalAvailablePowerForJobsOverlapping() {
		prepareMockManagement();
		final PowerJob mockPowerJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T08:00:00Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final PowerJob mockPowerJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T09:00:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob1, mockPowerJob2);
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(70);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 2 overlapping jobs before start")
	void testMinimalAvailablePowerForJobsOverlappingStart() {
		prepareMockManagement();
		final PowerJob mockPowerJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T06:00:00Z"))
				.endTime(Instant.parse("2022-01-01T09:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final PowerJob mockPowerJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T07:00:00Z"))
				.endTime(Instant.parse("2022-01-01T08:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(30).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob1, mockPowerJob2);
		final Instant startTime = Instant.parse("2022-01-01T07:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(50);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 2 overlapping jobs after end")
	void testMinimalAvailablePowerForJobsOverlappingEnd() {
		prepareMockManagement();
		final PowerJob mockPowerJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final PowerJob mockPowerJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T11:00:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(30).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob1, mockPowerJob2);
		final Instant startTime = Instant.parse("2022-01-01T07:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:30:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(50);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 2 overlapping jobs entire interval")
	void testMinimalAvailablePowerForJobsEntireInterval() {
		prepareMockManagement();
		final PowerJob mockPowerJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final PowerJob mockPowerJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T10:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(30).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob1, mockPowerJob2);
		final Instant startTime = Instant.parse("2022-01-01T10:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T12:00:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(50);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 2 non-overlapping jobs")
	void testMinimalAvailablePowerForNonOverlapping() {
		prepareMockManagement();
		final PowerJob mockPowerJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final PowerJob mockPowerJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T12:30:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(30).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob1, mockPowerJob2);
		final Instant startTime = Instant.parse("2022-01-01T10:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T12:45:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(70);
	}

	@Test
	@DisplayName("Test minimal available power calculation for 2 jobs with changing available capacity")
	void testMinimalAvailablePowerForTwoJobsChangingCapacity() {
		prepareMockManagement();
		doReturn(100.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isBefore(Instant.parse("2022-01-01T11:00:00Z")))));
		doReturn(150.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T11:00:00Z")))));
		final PowerJob mockPowerJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final PowerJob mockPowerJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T11:30:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(30).build();
		final Set<PowerJob> jobSet = Set.of(mockPowerJob1, mockPowerJob2);
		final Instant startTime = Instant.parse("2022-01-01T07:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T12:45:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(80);
	}

	@Test
	@DisplayName("Test minimal available power calculation for complicated scenario")
	void testMinimalAvailablePowerForComplicatedScenario() {
		prepareMockManagement();
		capacityForComplicatedScenario();
		final Set<PowerJob> jobSet = jobsForComplicatedMinimalPowerScenario();
		final Instant startTime = Instant.parse("2022-01-01T08:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T16:45:00Z");

		final double result = getMinimalAvailablePowerDuringTimeStamp(jobSet, startTime, endTime, MOCK_INTERVALS_LENGTH,
				POWER_MANAGEMENT, MOCK_MONITORING_DATA);

		assertThat(result).isEqualTo(65);
	}

	@Test
	@DisplayName("Test get next Fibonacci number")
	void testNextFibonacci() {
		final int previousNo = 8;
		final int result = nextFibonacci(previousNo);

		assertThat(result).isEqualTo(13);
	}

	@Test
	@DisplayName("Test get previous Fibonacci number")
	void testPreviousFibonacci() {
		final int previousNo = 89;
		final int result = previousFibonacci(previousNo);

		assertThat(result).isEqualTo(55);
	}

	private void capacityForComplicatedScenario() {
		doReturn(100.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isBefore(Instant.parse("2022-01-01T08:15:00Z")))));
		doReturn(200.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T08:15:00Z")))));
		doReturn(120.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T08:50:00Z")))));
		doReturn(220.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T09:00:00Z")))));
		doReturn(150.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T11:50:00Z")))));
		doReturn(230.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T12:30:00Z")))));
		doReturn(110.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(),
				argThat((instant -> instant.isAfter(Instant.parse("2022-01-01T15:00:00Z")))));
	}

	private Set<PowerJob> jobsForComplicatedMinimalPowerScenario() {
		final PowerJob mockJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T06:30:00Z"))
				.endTime(Instant.parse("2022-01-01T07:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(5).build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T07:00:00Z"))
				.endTime(Instant.parse("2022-01-01T08:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final PowerJob mockJob3 = ImmutablePowerJob.builder().jobId("3")
				.startTime(Instant.parse("2022-01-01T07:30:00Z"))
				.endTime(Instant.parse("2022-01-01T09:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();
		final PowerJob mockJob4 = ImmutablePowerJob.builder().jobId("4")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:55:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(2).build();
		final PowerJob mockJob5 = ImmutablePowerJob.builder().jobId("5")
				.startTime(Instant.parse("2022-01-01T08:50:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(15).build();
		final PowerJob mockJob6 = ImmutablePowerJob.builder().jobId("6")
				.startTime(Instant.parse("2022-01-01T12:00:00Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(70).build();
		final PowerJob mockJob7 = ImmutablePowerJob.builder().jobId("7")
				.startTime(Instant.parse("2022-01-01T12:30:00Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();

		return Set.of(mockJob1, mockJob2, mockJob3, mockJob4, mockJob5, mockJob6, mockJob7);
	}

	// Error calculation tests

	private void prepareMockManagement() {
		MOCK_MONITORING_DATA = ImmutableMonitoringData.builder().addWeatherData(
				ImmutableWeatherData.builder().cloudCover(5.5).temperature(10.0).windSpeed(20.0).time(MOCK_TIME)
						.build()).build();

		doReturn(MOCK_CAPACITY).when(POWER_MANAGEMENT).getCurrentMaximumCapacity();
		doReturn(MOCK_CAPACITY).when(POWER_MANAGEMENT).getInitialMaximumCapacity();
		doReturn(100.0).when(POWER_MANAGEMENT).getAvailablePower((MonitoringData) any(), any());
	}

	@Test
	@DisplayName("Test computing power calculation error for time intervals larger than 10 min")
	void testComputeIncorrectMaximumValProbabilityMoreThan10() {
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");
		final long interval = 20;

		assertThat(computeIncorrectMaximumValProbability(startTime, endTime, interval)).isEqualTo(0.51,
				Offset.offset(0D));
	}

	// Kendall Tau correlation test

	@Test
	@DisplayName("Test computing power calculation error for time intervals smaller than 10 min")
	void testComputeIncorrectMaximumValProbabilityLessThan10() {
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");
		final long interval = 5;

		assertThat(computeIncorrectMaximumValProbability(startTime, endTime, interval)).isEqualTo(0.01);
	}

	@ParameterizedTest
	@MethodSource("prepareKendallTauTest")
	@DisplayName("Test calculating the Kendall Tau correlation coefficient")
	void testComputeKendallTau(List<Instant> times, List<Double> values, double result) {

		assertThat(AlgorithmUtils.computeKendallTau(times, values)).isCloseTo(result, Offset.offset(0.04));
	}

	private static Stream<Arguments> prepareKendallTauTest() {
		final List<Instant> time = List.of(
				Instant.parse("2021-10-01T08:30:00Z"),
				Instant.parse("2021-09-01T08:30:00Z"),
				Instant.parse("2021-08-01T08:30:00Z"),
				Instant.parse("2021-07-01T08:30:00Z"),
				Instant.parse("2021-06-01T08:30:00Z"),
				Instant.parse("2021-06-01T08:30:00Z")
		);

		return Stream.of(
				Arguments.of(time, List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0), -1),
				Arguments.of(time, List.of(6.0, 5.0, 4.0, 3.0, 2.0, 1.0), 1),
				Arguments.of(time, List.of(6.0, 5.0, 6.0, 7.0, 2.0, 3.0), 0.35)
		);
	}
}
