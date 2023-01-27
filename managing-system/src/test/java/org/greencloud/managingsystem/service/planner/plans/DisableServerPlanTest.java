package org.greencloud.managingsystem.service.planner.plans;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.application.utils.TimeUtils;
import com.gui.agents.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

public class DisableServerPlanTest {

    @Mock
    ManagingAgent managingAgent;

    @Mock
    ManagingAgentNode managingAgentNode;

    @Mock
    private TimescaleDatabase timescaleDatabase;

    private DisableServerPlan disableServerPlan;



    @BeforeEach
    void setup() {
        managingAgent = mock(ManagingAgent.class);
        timescaleDatabase = mock(TimescaleDatabase.class);
        managingAgentNode = mock(ManagingAgentNode.class);

        disableServerPlan = new DisableServerPlan(managingAgent);
        doReturn(timescaleDatabase).when(managingAgentNode).getDatabaseClient();
        doReturn(managingAgentNode).when(managingAgent).getAgentNode();
        mockServerMonitoringData();
    }

    @Test
    @DisplayName("Test if the plan is executable")
    void testIsExecutable() {
        boolean result = disableServerPlan.isPlanExecutable();

        assertThat(result).isTrue();
    }

    @Test
	@Disabled
    @DisplayName("Test getting the target agent")
    void testGetTargetAgent() {
        var result = disableServerPlan.constructAdaptationPlan().getTargetAgent();

        assertThat(result).isEqualTo("server2");
    }

    private void mockServerMonitoringData() {
        var monitoringData1 = ImmutableServerMonitoringData.builder()
                .isDisabled(false)
                .currentMaximumCapacity(100)
                .currentTraffic(0)
                .successRatio(0.0)
                .currentBackUpPowerUsage(0)
                .build();
        var monitoringData2 = ImmutableServerMonitoringData.builder()
                .isDisabled(false)
                .currentMaximumCapacity(101)
                .currentTraffic(0)
                .successRatio(0.0)
                .currentBackUpPowerUsage(0)
                .build();
        var monitoringData3 = ImmutableServerMonitoringData.builder()
                .isDisabled(false)
                .currentMaximumCapacity(102)
                .currentTraffic(10.0)
                .successRatio(0.0)
                .currentBackUpPowerUsage(0)
                .build();
        var monitoringData4 = ImmutableServerMonitoringData.builder()
                .isDisabled(true)
                .currentMaximumCapacity(100)
                .currentTraffic(0)
                .successRatio(0.0)
                .currentBackUpPowerUsage(0)
                .build();
        var mockData = List.of(
                new AgentData(TimeUtils.getCurrentTime(), "server1", SERVER_MONITORING, monitoringData1),
                new AgentData(TimeUtils.getCurrentTime(), "server2", SERVER_MONITORING, monitoringData2),
                new AgentData(TimeUtils.getCurrentTime(), "server3", SERVER_MONITORING, monitoringData3),
                new AgentData(TimeUtils.getCurrentTime(), "server4", SERVER_MONITORING, monitoringData4)
        );
        doReturn(mockData).when(timescaleDatabase).readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));
    }
}
