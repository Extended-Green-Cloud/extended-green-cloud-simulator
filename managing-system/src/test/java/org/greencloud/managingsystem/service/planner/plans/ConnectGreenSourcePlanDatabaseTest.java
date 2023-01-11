package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

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
		connectGreenSourcePlan = new ConnectGreenSourcePlan(mockManagingAgent);
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@DisplayName("Test getting average traffic for servers for empty set")
	void testGetAverageTrafficForServersForEmptySet() {
		assertThat(connectGreenSourcePlan.getAverageTrafficForServers(emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Test getting average traffic for servers for distinct data set")
	void testGetAverageTrafficForServersForDistinctDataSet() {
		var mockData1 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.6)
				.successRatio(0.9)
				.currentBackUpPowerUsage(0.4)
				.build();
		var mockData2 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.8)
				.successRatio(0.9)
				.currentBackUpPowerUsage(0.4)
				.build();

		database.writeMonitoringData("test_server1", SERVER_MONITORING, mockData1);
		database.writeMonitoringData("test_server2", SERVER_MONITORING, mockData2);

		var result = connectGreenSourcePlan.getAverageTrafficForServers(List.of("test_server1", "test_server2"));

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_server1", 0.6, "test_server2", 0.8));
	}

	@Test
	@DisplayName("Test getting average traffic for servers for many rows data set")
	void testGetAverageTrafficForServersForManyRowsDataSet() {
		var mockData1 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.6)
				.successRatio(0.9)
				.currentBackUpPowerUsage(0.4)
				.build();
		var mockData2 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.8)
				.successRatio(0.9)
				.currentBackUpPowerUsage(0.4)
				.build();
		var mockData3 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.5)
				.successRatio(0.9)
				.currentBackUpPowerUsage(0.4)
				.build();

		database.writeMonitoringData("test_server1", SERVER_MONITORING, mockData1);
		database.writeMonitoringData("test_server2", SERVER_MONITORING, mockData2);
		database.writeMonitoringData("test_server2", SERVER_MONITORING, mockData3);

		var result = connectGreenSourcePlan.getAverageTrafficForServers(List.of("test_server1", "test_server2"));

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_server1", 0.6, "test_server2", 0.65));

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

	@Test
	@DisplayName("Test getting average traffic for sources for empty set")
	void testGetAverageTrafficForSourcesForEmptySet() {
		assertThat(connectGreenSourcePlan.getAverageTrafficForSources(emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Test getting average traffic for sources for distinct data set")
	void testGetAverageTrafficForSourcesForDistinctDataSet() {
		var mockData1 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.6)
				.successRatio(0.8)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(false)
				.build();
		var mockData2 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.4)
				.successRatio(0.8)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(true)
				.build();

		database.writeMonitoringData("test_gs1", GREEN_SOURCE_MONITORING, mockData1);
		database.writeMonitoringData("test_gs2", GREEN_SOURCE_MONITORING, mockData2);

		var result = connectGreenSourcePlan.getAverageTrafficForSources(List.of("test_gs1", "test_gs2"));

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_gs1", 0.6, "test_gs2", 0.4));
	}

	@Test
	@DisplayName("Test getting average traffic for sources for many rows data set")
	void testGetAverageTrafficForSourcesForManyRowsDataSet() {
		var mockData1 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.6)
				.successRatio(0.8)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(false)
				.build();
		var mockData2 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.4)
				.successRatio(0.8)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(true)
				.build();
		var mockData3 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.5)
				.successRatio(0.8)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(false)
				.build();

		database.writeMonitoringData("test_gs1", GREEN_SOURCE_MONITORING, mockData1);
		database.writeMonitoringData("test_gs2", GREEN_SOURCE_MONITORING, mockData2);
		database.writeMonitoringData("test_gs2", GREEN_SOURCE_MONITORING, mockData3);

		var result = connectGreenSourcePlan.getAverageTrafficForSources(List.of("test_gs1", "test_gs2"));

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_gs1", 0.6, "test_gs2", 0.45));

	}
}
