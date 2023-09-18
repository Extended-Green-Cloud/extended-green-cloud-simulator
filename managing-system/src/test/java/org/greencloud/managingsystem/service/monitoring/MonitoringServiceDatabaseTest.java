package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.managing.ManagingAgentNode;

@Disabled
class MonitoringServiceDatabaseTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;

	private MonitoringService monitoringService;
	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = TimescaleDatabase.setUpForTests();
		database.initDatabase();

		mockManagingAgent = spy(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		monitoringService = new MonitoringService(mockManagingAgent);

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(database).when(mockAgentNode).getDatabaseClient();
		doNothing().when(mockAgentNode).registerManagingAgent(anyList());
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@DisplayName("Test get adaptation goals from the database")
	void testReadSystemAdaptationGoals() {
		var expectedResult = List.of(
				new AdaptationGoal(1, "Maximize job success ratio", 0.8, true, 0.6),
				new AdaptationGoal(2, "Minimize used backup power", 0.2, false, 0.2),
				new AdaptationGoal(3, "Distribute traffic evenly", 1.0, false, 0.2)
		);
		monitoringService.readSystemAdaptationGoals();

		var result = mockManagingAgent.getAdaptationGoalList();

		assertThat(result)
				.as("Managing agent should have 3 goals")
				.hasSize(3)
				.as("Data of the goals should equal to the expected result")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	@Test
	@DisplayName("Test get alive agents for green sources")
	void testGetAliveAgentsForGreenSources() {
		mockHealthCheckData();
		var result = monitoringService.getAliveAgents(GREEN_ENERGY);

		assertThat(result)
				.hasSize(3)
				.matches((data) -> List.of("test_gs1", "test_gs2", "test_gs3").containsAll(data));
	}

	@Test
	@DisplayName("Test get average values for agents")
	void testGetAverageValuesForAgents() {
		mockGreenSourceData();

		ToDoubleFunction<AgentData> testFunction = data ->
				((GreenSourceMonitoringData) data.monitoringData()).getCurrentTraffic();
		List<String> agentsOfInterest = List.of("test_gs1", "test_gs2", "test_gs3", "test_gs4");

		var result = monitoringService.getAverageValuesForAgents(GREEN_SOURCE_MONITORING, agentsOfInterest,
				testFunction);
		var expectedMap = Map.of(
				"test_gs1", 0.7,
				"test_gs2", 0.55,
				"test_gs3", 0.0,
				"test_gs4", 0.0
		);

		assertThat(result)
				.as("Result has size equal to 4")
				.hasSize(4)
				.containsExactlyInAnyOrderEntriesOf(expectedMap);

	}

	@Test
	@DisplayName("Test getting average traffic for network components for empty set")
	void testGetAverageTrafficForNetworkComponentForEmptySet() {
		assertThat(monitoringService.getAverageTrafficForNetworkComponent(emptyList(), SERVER_MONITORING)).isEmpty();
	}

	@Test
	@DisplayName("Test getting average traffic for network components for distinct data set")
	void testGetAverageTrafficForNetworkComponentForDistinctDataSet() {
		var mockData1 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.6)
				.serverJobs(10)
				.successRatio(0.9)
				.isDisabled(false)
				.build();
		var mockData2 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.8)
				.successRatio(0.9)
				.serverJobs(10)
				.isDisabled(false)
				.build();

		database.writeMonitoringData("test_server1", SERVER_MONITORING, mockData1);
		database.writeMonitoringData("test_server2", SERVER_MONITORING, mockData2);

		var result = monitoringService.getAverageTrafficForNetworkComponent(List.of("test_server1", "test_server2"),
				SERVER_MONITORING);

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_server1", 0.6, "test_server2", 0.8));
	}

	@Test
	@DisplayName("Test getting average traffic for network components for many rows data set")
	void testGetAverageTrafficForNetworkComponentForManyRowsDataSet() {
		var mockData1 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.6)
				.successRatio(0.9)
				.serverJobs(10)
				.isDisabled(false)
				.build();
		var mockData2 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.8)
				.successRatio(0.9)
				.serverJobs(10)
				.isDisabled(false)
				.build();
		var mockData3 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.5)
				.successRatio(0.9)
				.serverJobs(10)
				.isDisabled(false)
				.build();

		database.writeMonitoringData("test_server1", SERVER_MONITORING, mockData1);
		database.writeMonitoringData("test_server2", SERVER_MONITORING, mockData2);
		database.writeMonitoringData("test_server2", SERVER_MONITORING, mockData3);

		var result = monitoringService.getAverageTrafficForNetworkComponent(List.of("test_server1", "test_server2"),
				SERVER_MONITORING);

		assertThat(result)
				.hasSize(2)
				.containsExactlyInAnyOrderEntriesOf(Map.of("test_server1", 0.6, "test_server2", 0.65));

	}

	private void mockHealthCheckData() {
		var healthCheck1 = new HealthCheck(true, GREEN_ENERGY);
		var healthCheck2 = new HealthCheck(true, GREEN_ENERGY);
		var healthCheck3 = new HealthCheck(true, GREEN_ENERGY);

		var mockData = List.of(
				new AgentData(now(), "test_gs1", HEALTH_CHECK, healthCheck1),
				new AgentData(now(), "test_gs2", HEALTH_CHECK, healthCheck2),
				new AgentData(now(), "test_gs3", HEALTH_CHECK, healthCheck3)
		);

		mockData.forEach(data -> database.writeMonitoringData(data.aid(), data.dataType(), data.monitoringData()));
	}

	private void mockGreenSourceData() {
		var data1 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.7)
				.weatherPredictionError(0.02)
				.successRatio(0.8)
				.isBeingDisconnected(false)
				.build();
		var data2 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.5)
				.weatherPredictionError(0.02)
				.successRatio(0.8)
				.isBeingDisconnected(false)
				.build();
		var data3 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.6)
				.weatherPredictionError(0.02)
				.successRatio(0.8)
				.isBeingDisconnected(false)
				.build();

		var mockData = List.of(
				new AgentData(now(), "test_gs1", GREEN_SOURCE_MONITORING, data1),
				new AgentData(now(), "test_gs2", GREEN_SOURCE_MONITORING, data2),
				new AgentData(now(), "test_gs2", GREEN_SOURCE_MONITORING, data3)
		);

		mockData.forEach(data -> database.writeMonitoringData(data.aid(), data.dataType(), data.monitoringData()));

	}
}
