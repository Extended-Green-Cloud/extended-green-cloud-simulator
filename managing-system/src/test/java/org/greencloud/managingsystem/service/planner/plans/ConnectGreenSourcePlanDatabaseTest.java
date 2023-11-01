package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.connector.gui.agents.managing.ManagingAgentNode;

@Disabled
class ConnectGreenSourcePlanDatabaseTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;
	private TimescaleDatabase database;

	private ConnectGreenSourcePlan connectGreenSourcePlan;

	@BeforeEach
	void init() {
		database = spy(TimescaleDatabase.setUpForTests());
		database.initDatabase();

		mockManagingAgent = spy(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);

		doReturn(new MonitoringService(mockManagingAgent)).when(mockManagingAgent).monitor();
		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(database).when(mockAgentNode).getDatabaseClient();
		connectGreenSourcePlan = new ConnectGreenSourcePlan(mockManagingAgent, MAXIMIZE_JOB_SUCCESS_RATIO);
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}


	@Test
	@DisplayName("Test getting average power for sources for empty set")
	void testGetAveragePowerForSourcesForEmptySet() {
		assertThat(connectGreenSourcePlan.getAveragePowerForSources(emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Test getting average power for sources for distinct data set")
	void testGetAveragePowerForSourcesForDistinctDataSet() {
		var mockData1 = new AvailableGreenEnergy(0.7);
		var mockData2 = new AvailableGreenEnergy(0.9);

		database.writeMonitoringData("test_gs1", AVAILABLE_GREEN_ENERGY, mockData1);
		database.writeMonitoringData("test_gs2", AVAILABLE_GREEN_ENERGY, mockData2);

		var result = connectGreenSourcePlan.getAveragePowerForSources(List.of("test_gs1", "test_gs2"));

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_gs1", 0.7, "test_gs2", 0.9));
	}

	@Test
	@Disabled
	@DisplayName("Test getting average power for sources for many rows data set")
	void testGetAveragePowerForSourcesForManyRowsDataSet() {
		var mockData1 = new AvailableGreenEnergy(0.7);
		var mockData2 = new AvailableGreenEnergy(0.9);
		var mockData3 = new AvailableGreenEnergy(0.3);

		database.writeMonitoringData("test_gs1", AVAILABLE_GREEN_ENERGY, mockData1);
		database.writeMonitoringData("test_gs2", AVAILABLE_GREEN_ENERGY, mockData2);
		database.writeMonitoringData("test_gs1", AVAILABLE_GREEN_ENERGY, mockData3);

		var result = connectGreenSourcePlan.getAveragePowerForSources(List.of("test_gs1", "test_gs2"));

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_gs1", 0.5, "test_gs2", 0.9));

	}
}
