package com.greencloud.application.agents.scheduler;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.gui.agents.SchedulerAgentNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;

public class AbstractSchedulerAgentTest {
	SchedulerAgent schedulerAgent;
	SchedulerAgentNode schedulerAgentNode;

	@BeforeEach
	void init() {
		schedulerAgent = spy(SchedulerAgent.class);
		schedulerAgentNode = mock(SchedulerAgentNode.class);

		doReturn(schedulerAgentNode).when(schedulerAgent).getAgentNode();
		schedulerAgent.configManagement = new SchedulerConfigurationManagement(schedulerAgent, 1, 1, 10000, 100, 2);
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
}
