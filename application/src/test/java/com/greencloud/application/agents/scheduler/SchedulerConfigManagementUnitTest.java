package com.greencloud.application.agents.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

public class SchedulerConfigManagementUnitTest {

	private ClientJob mockJob = ImmutableClientJob.builder()
			.jobId("1")
			.clientIdentifier("Client1")
			.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
			.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
			.deadline(Instant.parse("2022-01-01T12:00:00.000Z"))
			.power(100)
			.build();

	private SchedulerConfigurationManagement schedulerConfigManagement;

	@BeforeEach
	void setUp() {
		schedulerConfigManagement = new SchedulerConfigurationManagement(0.7, 0.3, 100);
	}

	@Test
	@DisplayName("Test computation of job priority")
	void testGetJobPriority() {
		final double expectedResult = 5040030;
		assertThat(schedulerConfigManagement.getJobPriority(mockJob)).isEqualTo(expectedResult);
	}
}
