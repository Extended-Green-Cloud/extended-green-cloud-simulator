package com.greencloud.application.utils;

import static com.greencloud.application.utils.AlgorithmUtils.findJobsWithinPower;
import static com.greencloud.application.utils.AlgorithmUtils.getMaximumUsedPowerDuringTimeStamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.job.ImmutableJob;
import com.greencloud.application.domain.job.ImmutablePowerJob;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.PowerJob;

class AlgorithmUtilsUnitTest {

	// GETTING MAXIMUM POWER WITHIN TIMESTAMP TESTS

	@Test
	@DisplayName("Test maximum capacity calculation for no jobs")
	void testMaximumCapacityEmptyJobs() {
		final Set<Job> jobList = Collections.EMPTY_SET;
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 1 Job")
	void testMaximumCapacityForOneJob() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(10).build();
		final Set<Job> jobList = Set.of(mockJob1);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T10:30:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(10);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs overlapping in time interval")
	void testMaximumCapacityForTwoOverlappingJobs() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(10).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T10:30:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z")).power(15).build();

		final Set<Job> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs starting before time interval")
	void testMaximumCapacityForJobStartingBeforeTimeInterval() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(10).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T07:30:00Z"))
				.endTime(Instant.parse("2022-01-01T10:15:00Z")).power(15).build();

		final Set<Job> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs finishing after time interval")
	void testMaximumCapacityForJobFinishingAfterTimeInterval() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T10:30:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z")).power(10).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T10:45:00Z"))
				.endTime(Instant.parse("2022-01-01T12:15:00Z")).power(15).build();

		final Set<Job> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 Jobs overlapping time interval")
	void testMaximumCapacityForJobOverlappingTimeInterval() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(10).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T09:15:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(15).build();

		final Set<Job> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(25);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 3 Jobs with 1 outside interval")
	void testMaximumCapacityForJobOutsideInterval() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:10:00Z")).power(10).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T10:15:00Z"))
				.endTime(Instant.parse("2022-01-01T11:30:00Z")).power(20).build();
		final Job mockJob3 = ImmutableJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T07:15:00Z"))
				.endTime(Instant.parse("2022-01-01T08:30:00Z")).power(15).build();

		final Set<Job> jobList = Set.of(mockJob1, mockJob2, mockJob3);
		final Instant startTime = Instant.parse("2022-01-01T08:30:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:00:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(30);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for 2 non-overlapping Jobs")
	void testMaximumCapacityForNoOverlappingJobs() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(10).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:15:00Z")).power(15).build();

		final Set<Job> jobList = Set.of(mockJob1, mockJob2);
		final Instant startTime = Instant.parse("2022-01-01T09:00:00Z");
		final Instant endTime = Instant.parse("2022-01-01T11:30:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(15);
	}

	@Test
	@DisplayName("Test maximum capacity calculation for complicated scenario")
	void testMaximumCapacityForComplicatedScenario() {
		final Set<Job> jobList = jobsForFirstComplicatedScenario();
		final Instant startTime = Instant.parse("2022-01-01T08:45:00Z");
		final Instant endTime = Instant.parse("2022-01-01T12:45:00Z");

		final int result = getMaximumUsedPowerDuringTimeStamp(jobList, startTime, endTime);

		assertThat(result).isEqualTo(15);
	}

	private Set<Job> jobsForFirstComplicatedScenario() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(1).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:00:00Z"))
				.endTime(Instant.parse("2022-01-01T09:00:00Z")).power(2).build();
		final Job mockJob3 = ImmutableJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T08:40:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(3).build();
		final Job mockJob4 = ImmutableJob.builder().jobId("4").clientIdentifier("Test Client 4")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z")).power(4).build();
		final Job mockJob5 = ImmutableJob.builder().jobId("5").clientIdentifier("Test Client 5")
				.startTime(Instant.parse("2022-01-01T10:45:00Z"))
				.endTime(Instant.parse("2022-01-01T11:30:00Z")).power(5).build();
		final Job mockJob6 = ImmutableJob.builder().jobId("6").clientIdentifier("Test Client 6")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z")).power(6).build();
		final Job mockJob7 = ImmutableJob.builder().jobId("7").clientIdentifier("Test Client 7")
				.startTime(Instant.parse("2022-01-01T12:15:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z")).power(7).build();
		return Set.of(mockJob1, mockJob2, mockJob3, mockJob4, mockJob5, mockJob6, mockJob7);
	}

	// GETTING JOBS WITHING POWER TESTS

	@Test
	@DisplayName("Jobs within power for empty list")
	void testJobsWithinPowerForEmptyList() {
		final int maxPower = 10;

		final List<Job> result = findJobsWithinPower(Collections.emptyList(), maxPower, Job.class);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Jobs within power for 1 job")
	void testJobsWithinPowerForOneJob() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(5).build();
		final List<Job> mockJobs = List.of(mockJob1);
		final int maxPower = 10;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Jobs within power for 1 job with too much power")
	void testJobsWithinPowerForOneJobTooMuchPower() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(15).build();
		final List<Job> mockJobs = List.of(mockJob1);
		final int maxPower = 10;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Jobs within power for two jobs with too much power")
	void testJobsWithinPowerForTwoJobsTooMuchPower() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(7).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(15).build();
		final List<Job> mockJobs = List.of(mockJob1, mockJob2);
		final int maxPower = 10;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Jobs within power for two jobs with one greater power")
	void testJobsWithinPowerForTwoJobsOneGreaterPower() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(7).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(8).build();
		final List<Job> mockJobs = List.of(mockJob1, mockJob2);
		final int maxPower = 10;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getJobId()).isEqualTo("2");
	}

	@Test
	@DisplayName("Jobs within power for two jobs both included")
	void testJobsWithinPowerForTwoJobsBothInsidePower() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(7).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(8).build();
		final List<Job> mockJobs = List.of(mockJob1, mockJob2);
		final int maxPower = 15;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(2);
		assertThat(result.get(1).getJobId()).isEqualTo("2");
	}

	@Test
	@DisplayName("Jobs within power for 3 jobs all within power")
	void testJobsWithinPowerForThreeJobsWithinPower() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(5).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(7).build();
		final Job mockJob3 = ImmutableJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(4).build();
		final List<Job> mockJobs = List.of(mockJob1, mockJob2, mockJob3);
		final int maxPower = 15;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);

		assertFalse(result.isEmpty());
		assertThat(result).hasSize(2);
		assertTrue(result.containsAll(List.of(mockJob1, mockJob2)));
	}

	@Test
	@DisplayName("Jobs within power for complicated scenario")
	void testJobsWithinPowerForComplicatedScenario() {
		final List<Job> mockJobs = jobsForComplicatedPowerScenario();
		final int maxPower = 25;

		final List<Job> result = findJobsWithinPower(mockJobs, maxPower, Job.class);
		final int resultSum = result.stream().mapToInt(Job::getPower).sum();

		assertFalse(result.isEmpty());
		assertThat(resultSum).isEqualTo(25);
	}

	@Test
	@DisplayName("Jobs within power for power job")
	void testJobsWithinPowerForPowerJob() {
		final PowerJob mockJob1 = ImmutablePowerJob.builder().jobId("1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(5).build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder().jobId("2")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(7).build();
		final PowerJob mockJob3 = ImmutablePowerJob.builder().jobId("3")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(4).build();
		final List<PowerJob> mockJobs = List.of(mockJob1, mockJob2, mockJob3);
		final int maxPower = 10;

		final List<PowerJob> result = findJobsWithinPower(mockJobs, maxPower, PowerJob.class);
		final int resultSum = result.stream().mapToInt(PowerJob::getPower).sum();

		assertFalse(result.isEmpty());
		assertThat(resultSum).isEqualTo(9);
		assertThat(result).hasSize(2);
		assertTrue(result.containsAll(List.of(mockJob1, mockJob3)));
	}

	private List<Job> jobsForComplicatedPowerScenario() {
		final Job mockJob1 = ImmutableJob.builder().jobId("1").clientIdentifier("Test Client 1")
				.startTime(Instant.parse("2022-01-01T08:30:00Z"))
				.endTime(Instant.parse("2022-01-01T08:45:00Z")).power(5).build();
		final Job mockJob2 = ImmutableJob.builder().jobId("2").clientIdentifier("Test Client 2")
				.startTime(Instant.parse("2022-01-01T08:00:00Z"))
				.endTime(Instant.parse("2022-01-01T09:00:00Z")).power(10).build();
		final Job mockJob3 = ImmutableJob.builder().jobId("3").clientIdentifier("Test Client 3")
				.startTime(Instant.parse("2022-01-01T08:40:00Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00Z")).power(3).build();
		final Job mockJob4 = ImmutableJob.builder().jobId("4").clientIdentifier("Test Client 4")
				.startTime(Instant.parse("2022-01-01T10:00:00Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00Z")).power(2).build();
		final Job mockJob5 = ImmutableJob.builder().jobId("5").clientIdentifier("Test Client 5")
				.startTime(Instant.parse("2022-01-01T10:45:00Z"))
				.endTime(Instant.parse("2022-01-01T11:30:00Z")).power(16).build();
		final Job mockJob6 = ImmutableJob.builder().jobId("6").clientIdentifier("Test Client 6")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z")).power(26).build();
		final Job mockJob7 = ImmutableJob.builder().jobId("7").clientIdentifier("Test Client 7")
				.startTime(Instant.parse("2022-01-01T12:15:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z")).power(7).build();
		final Job mockJob8 = ImmutableJob.builder().jobId("8").clientIdentifier("Test Client 8")
				.startTime(Instant.parse("2022-01-01T11:15:00Z"))
				.endTime(Instant.parse("2022-01-01T12:30:00Z")).power(8).build();
		final Job mockJob9 = ImmutableJob.builder().jobId("9").clientIdentifier("Test Client 9")
				.startTime(Instant.parse("2022-01-01T12:15:00Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00Z")).power(14).build();
		return List.of(mockJob1, mockJob2, mockJob3, mockJob4, mockJob5, mockJob6, mockJob7, mockJob8, mockJob9);
	}
}
