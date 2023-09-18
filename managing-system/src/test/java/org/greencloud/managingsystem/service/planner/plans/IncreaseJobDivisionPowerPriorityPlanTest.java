package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static java.time.Instant.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.timescale.TimescaleDatabase;
import org.greencloud.commons.args.agent.AgentType;
import com.gui.agents.managing.ManagingAgentNode;

class IncreaseJobDivisionPowerPriorityPlanTest {

	@Mock
	private ManagingAgent managingAgent;

	@Mock
	private ManagingAgentNode managingAgentNode;

	@Mock
	private TimescaleDatabase timescaleDatabase;

	private IncreaseJobDivisionPowerPriorityPlan increaseJobDivisionPowerPriorityPlan;

	@BeforeEach
	void init() {
		managingAgent = mock(ManagingAgent.class);
		timescaleDatabase = mock(TimescaleDatabase.class);
		managingAgentNode = mock(ManagingAgentNode.class);

		increaseJobDivisionPowerPriorityPlan = new IncreaseJobDivisionPowerPriorityPlan(managingAgent,
				MAXIMIZE_JOB_SUCCESS_RATIO);
		doReturn(timescaleDatabase).when(managingAgentNode).getDatabaseClient();
		doReturn(managingAgentNode).when(managingAgent).getAgentNode();
		doReturn(new MonitoringService(managingAgent)).when(managingAgent).monitor();
		mockHealthCheckData();
	}

	@Test
	@DisplayName("Test if the plan is executable")
	void testIsExecutable() {
		var result = increaseJobDivisionPowerPriorityPlan.isPlanExecutable();

		assertThat(result).isTrue();
	}

	private void mockHealthCheckData() {
		var healthCheck = new HealthCheck(true, AgentType.SCHEDULER);
		var mockData = List.of(new AgentData(now(), "schedulerTest", HEALTH_CHECK, healthCheck));
		doReturn(mockData).when(timescaleDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);
	}
}
