package org.greencloud.managingsystem.service.planner.plans;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.agent.AgentType;
import com.gui.agents.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class IncreaseDeadlinePriorityPlanTest {

    @Mock
    private ManagingAgent managingAgent;
    @Mock
    private ManagingAgentNode managingAgentNode;
    @Mock
    private TimescaleDatabase timescaleDatabase;

    private IncreaseDeadlinePriorityPlan increaseDeadlinePriorityPlan;

    @BeforeEach
    void init() {
        managingAgent = mock(ManagingAgent.class);
        timescaleDatabase = mock(TimescaleDatabase.class);
        managingAgentNode = mock(ManagingAgentNode.class);

        increaseDeadlinePriorityPlan = new IncreaseDeadlinePriorityPlan(managingAgent);
        doReturn(timescaleDatabase).when(managingAgentNode).getDatabaseClient();
        doReturn(managingAgentNode).when(managingAgent).getAgentNode();
        mockHealthCheckData();
    }

    @Test
    @DisplayName("Test if the plan is executable")
    void testIsExecutable() {
        var result = increaseDeadlinePriorityPlan.isPlanExecutable();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Test is scheduler alive")
    void testIsSchedulerAlive() {
        var result = increaseDeadlinePriorityPlan.isSchedulerAlive(
                timescaleDatabase.readMonitoringDataForDataTypes(
                        Collections.singletonList(HEALTH_CHECK),
                        MONITOR_SYSTEM_DATA_HEALTH_PERIOD
                )
        );

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Test getting the target scheduler agent")
    void testGetTargetSchedulerAgent() {
        var result = increaseDeadlinePriorityPlan.getTargetScheduler(
                timescaleDatabase.readMonitoringDataForDataTypes(
                        Collections.singletonList(HEALTH_CHECK),
                        MONITOR_SYSTEM_DATA_HEALTH_PERIOD
                ));

        assertThat(result).isEqualTo("schedulerTest");
    }

    private void mockHealthCheckData() {
        var healthCheck = new HealthCheck(true, AgentType.SCHEDULER);
        var mockData = List.of(
                new AgentData(now(), "schedulerTest", HEALTH_CHECK, healthCheck)
        );
        doReturn(mockData).when(timescaleDatabase).readMonitoringDataForDataTypes(Collections.singletonList(HEALTH_CHECK),
                MONITOR_SYSTEM_DATA_HEALTH_PERIOD);
    }
}