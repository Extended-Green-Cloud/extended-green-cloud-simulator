package org.greencloud.managingsystem.service.monitoring.goalservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.timescale.TimescaleDatabase;

class TrafficDistributionServiceTest {

	@Mock
	private ManagingAgent mockManagingAgent;

	@Mock
	private ManagingAgentNode mockAgentNode;

	@Mock
	private TimescaleDatabase mockDatabase;

	@Mock
	private MonitoringService mockMonitoringService;

	private TrafficDistributionService trafficDistributionService;

	@BeforeEach
	void setUp() {
		mockManagingAgent = mock(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		mockDatabase = mock(TimescaleDatabase.class);
		mockMonitoringService = mock(MonitoringService.class);

		trafficDistributionService = new TrafficDistributionService(mockManagingAgent);

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(mockDatabase).when(mockAgentNode).getDatabaseClient();
		doReturn(mockMonitoringService).when(mockManagingAgent).monitor();
	}

	@Test
	@DisplayName("Test compute coefficient")
	void testComputeCoefficient() {
		double coefficient = trafficDistributionService.computeCoefficient(List.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(coefficient).isEqualTo(0.5270462766947299);
	}

}
