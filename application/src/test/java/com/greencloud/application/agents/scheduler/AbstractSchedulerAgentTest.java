package com.greencloud.application.agents.scheduler;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.commons.domain.job.ImmutableClientJob;
import com.gui.agents.SchedulerAgentNode;

class AbstractSchedulerAgentTest {
	SchedulerAgent schedulerAgent;
	SchedulerAgentNode schedulerAgentNode;

	@BeforeEach
	void init() {
		schedulerAgent = spy(SchedulerAgent.class);
		schedulerAgentNode = mock(SchedulerAgentNode.class);

		doReturn(schedulerAgentNode).when(schedulerAgent).getAgentNode();
		schedulerAgent.configManagement = new SchedulerConfigurationManagement(schedulerAgent, 1, 1, 10000, 100, 2);
		schedulerAgent.setUpPriorityQueue();
	}

	@Test
	@DisplayName("Test executing adaptation action for incrementing deadline weight")
	void testExecuteIncreaseDeadline() {
		var adaptationAction = getAdaptationAction(AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY);
		schedulerAgent.executeAction(adaptationAction, null);

		assertThat(schedulerAgent.configManagement.getDeadlineWeightPriority()).isEqualTo(0.6666666666666666);
	}

	@Test
	@DisplayName("Test executing adaptation action for incrementing power division weight")
	void testExecuteIncreasePowerDivision() {
		var adaptationAction = getAdaptationAction(AdaptationActionEnum.INCREASE_POWER_PRIORITY);
		schedulerAgent.executeAction(adaptationAction, null);

		assertThat(schedulerAgent.configManagement.getDeadlineWeightPriority()).isEqualTo(0.3333333333333333);
	}

	@Test
	@DisplayName("Test adding job to the queue - test deadline priority")
	void testAddJobToTheQueueDeadlinePriority() {
		var mockJob = ImmutableClientJob.builder().jobId("1").clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z")).endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T12:00:00.000Z")).power(100).build();
		var mockJob2 = ImmutableClientJob.builder().jobId("2").clientIdentifier("Client2")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z")).endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T14:00:00.000Z")).power(100).build();

		schedulerAgent.getJobsToBeExecuted().put(mockJob2);
		schedulerAgent.getJobsToBeExecuted().put(mockJob);

		assertThat(schedulerAgent.getJobsToBeExecuted().peek()).isEqualTo(mockJob);
	}

	@Test
	@DisplayName("Test adding job to the queue - test power priority")
	void testAddJobToTheQueuePowerPriority() {
		var mockJob = ImmutableClientJob.builder().jobId("1").clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z")).endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T12:00:00.000Z")).power(100).build();
		var mockJob2 = ImmutableClientJob.builder().jobId("1").clientIdentifier("Client1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z")).endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T12:00:00.000Z")).power(110).build();

		schedulerAgent.getJobsToBeExecuted().put(mockJob2);
		schedulerAgent.getJobsToBeExecuted().put(mockJob);

		assertThat(schedulerAgent.getJobsToBeExecuted().peek()).isEqualTo(mockJob);
	}
}
