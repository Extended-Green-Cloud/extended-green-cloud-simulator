package com.greencloud.application.agents.scheduler.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.ImmutableClientJob;
import com.gui.agents.SchedulerAgentNode;

class SchedulerConfigManagementUnitTest {

	private final ClientJob mockJob = ImmutableClientJob.builder().jobId("1").clientIdentifier("Client1")
			.startTime(Instant.parse("2022-01-01T08:00:00.000Z")).endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
			.deadline(Instant.parse("2022-01-01T12:00:00.000Z")).power(100).build();

	private SchedulerConfigurationManagement schedulerConfigManagement;

	@BeforeEach
	void setUp() {
		SchedulerAgent mockSchedulerAgent = spy(SchedulerAgent.class);
		SchedulerAgentNode mockSchedulerAgentNode = mock(SchedulerAgentNode.class);
		doReturn(mockSchedulerAgentNode).when(mockSchedulerAgent).getAgentNode();
		schedulerConfigManagement = new SchedulerConfigurationManagement(mockSchedulerAgent, 1, 1, 100, 1000, 2);
	}

	@Test
	@DisplayName("Test computation of job priority")
	void testGetJobPriority() {
		final double expectedResult = 3600050;
		assertThat(schedulerConfigManagement.getJobPriority(mockJob)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Test increase job deadline priority")
	void testIncrementDeadline() {
		for (int i = 0; i < 5; i++) {
			schedulerConfigManagement.increaseDeadlineWeight();
		}
		assertThat(schedulerConfigManagement.getDeadlineWeightPriority()).isEqualTo(13.0 / 14);
	}

	@Test
	@DisplayName("Test increase job power division priority")
	void testIncrementPowerDivision() {
		for (int i = 0; i < 5; i++) {
			schedulerConfigManagement.increasePowerWeight();
		}
		assertThat(schedulerConfigManagement.getPowerWeightPriority()).isEqualTo(13.0 / 14);
	}
}
