package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsBackUpPower;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsPowerShortages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.args.agent.cloudnetwork.ImmutableCloudNetworkArgs;
import com.greencloud.commons.managingsystem.planner.AdjustGreenSourceErrorParameters;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;

class DecrementGreenSourceErrorPlanUnitTest {

	private final static List<String> GREEN_SOURCES = List.of("test_gs1", "test_gs2", "test_gs3", "test_gs4");

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ScenarioStructureArgs mockScenarioStructure;
	@Mock
	private ManagingAgentNode mockAgentNode;
	@Mock
	private TimescaleDatabase mockDatabase;
	@Mock
	private MonitoringService mockMonitoring;

	private DecrementGreenSourceErrorPlan decrementGreenSourceErrorPlan;

	@BeforeEach
	void init() {
		mockManagingAgent = mock(ManagingAgent.class);
		mockScenarioStructure = mock(ScenarioStructureArgs.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		mockDatabase = mock(TimescaleDatabase.class);
		mockMonitoring = spy(new MonitoringService(mockManagingAgent));

		decrementGreenSourceErrorPlan = new DecrementGreenSourceErrorPlan(mockManagingAgent);

		doReturn(mockMonitoring).when(mockManagingAgent).monitor();
		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(mockDatabase).when(mockAgentNode).getDatabaseClient();

		doReturn(prepareGreenSourceWeatherShortageData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, GREEN_SOURCES, MONITOR_SYSTEM_DATA_TIME_PERIOD);
		doReturn(prepareGreenSourceMonitoringData()).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING));
		doReturn(GREEN_SOURCES).when(mockMonitoring).getAliveAgents(GREEN_SOURCE);

		prepareNetworkStructure();
	}

	@Test
	@DisplayName("Test getting green sources having the prediction error within bounds")
	void testGetGreenSourcesWithErrorInBounds() {
		// when
		var result = decrementGreenSourceErrorPlan.getGreenSourcesWithErrorInBounds(GREEN_SOURCES);

		// then
		assertThat(result)
				.as("Retrieve data has correct size equal 3")
				.hasSize(3)
				.as("Retrieve data contains correct fields")
				.containsExactly("test_gs1", "test_gs3", "test_gs4");
	}

	@Test
	@DisplayName("Test getting green sources having the correct power shortage count")
	void testGetGreenSourcesWithCorrectPowerShortageCount() {
		// when
		var result = decrementGreenSourceErrorPlan.getGreenSourcesWithCorrectPowerShortageCount(GREEN_SOURCES);

		// then
		var expectedResult = List.of(
				new AgentsPowerShortages("test_gs1", 1),
				new AgentsPowerShortages("test_gs4", 1)
		);

		assertThat(result)
				.as("Retrieve data has correct size equal 2")
				.hasSize(2)
				.as("Retrieve data contains correct fields")
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	@Test
	@DisplayName("Test getting valid for adaptation green sources for server")
	void testGetValidGreenSourcesForServer() {
		// given
		var data1 = new WeatherShortages(1, 2000);
		var monitoringData = List.of(new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, data1));
		doReturn(monitoringData).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, List.of("test_gs1"),
						MONITOR_SYSTEM_DATA_TIME_PERIOD);

		var testServer = new AgentsBackUpPower("test_server1", 0.7);
		var aliveAgents = List.of("test_gs1", "test_gs2", "test_gs3");

		// when
		var result =
				decrementGreenSourceErrorPlan.getValidGreenSourcesForServer(testServer, aliveAgents);

		// then
		var expectedResult = List.of(
				new AgentsPowerShortages("test_gs1", 1)
		);

		assertThat(result.getKey()).isEqualTo(testServer);
		assertThat(result.getValue())
				.as("Retrieve data has correct size equal 1")
				.hasSize(1)
				.as("Retrieve data contains correct fields")
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	@Test
	@DisplayName("Test getting green sources per servers")
	void testGetGreenSourcesPerServers() {
		// given
		var data1 = new WeatherShortages(1, 2000);
		var data3 = new WeatherShortages(3, 2000);
		var data4 = new WeatherShortages(1, 2000);

		var monitoringData1 = List.of(new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, data1));
		var monitoringData2 = List.of(
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, data3),
				new AgentData(now(), "test_gs4", WEATHER_SHORTAGES, data4)
		);

		doReturn(monitoringData1).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, List.of("test_gs1"),
						MONITOR_SYSTEM_DATA_TIME_PERIOD);
		doReturn(monitoringData2).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, List.of("test_gs3", "test_gs4"),
						MONITOR_SYSTEM_DATA_TIME_PERIOD);

		var testServer1 = new AgentsBackUpPower("test_server1", 0.7);
		var testServer2 = new AgentsBackUpPower("test_server2", 0.8);
		var testServers = List.of(testServer1, testServer2);

		// when
		var result = decrementGreenSourceErrorPlan.getGreenSourcesPerServers(testServers);

		// then
		var expectedResult = Map.of(
				testServer1, List.of(new AgentsPowerShortages("test_gs1", 1)),
				testServer2, List.of(new AgentsPowerShortages("test_gs4", 1))
		);

		assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedResult);
	}

	@Test
	@DisplayName("Test getting valid servers for the plan")
	void testGetConsideredServers() {
		// given
		doReturn(List.of("test_server1", "test_server2")).when(mockMonitoring).getAliveAgents(SERVER);
		doReturn(prepareServerMonitoringData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(SERVER_MONITORING), eq(List.of("test_server1", "test_server2")),
						anyDouble());
		var threshold = 0.7;

		// when
		var result = decrementGreenSourceErrorPlan.getConsideredServers(threshold);

		// then
		var expectedResult = List.of(
				new AgentsBackUpPower("test_server1", 0.75)
		);

		assertThat(result)
				.as("Retrieve data has correct size equal 1")
				.hasSize(1)
				.as("Retrieve data contains correct fields")
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	@Test
	@DisplayName("Test is plan executable for no available servers")
	void testIsPlanExecutableForNoServers() {
		// given
		var testAdaptationGoal = new AdaptationGoal(2, "MINIMIZE_USED_BACKUP_POWER", 0.9, true, 0.3);

		doReturn(testAdaptationGoal).when(mockMonitoring).getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER);
		doReturn(List.of("test_server1", "test_server2")).when(mockMonitoring).getAliveAgents(SERVER);
		doReturn(prepareServerMonitoringData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(SERVER_MONITORING, List.of("test_server1", "test_server2"),
						MONITOR_SYSTEM_DATA_TIME_PERIOD);

		// when
		var result = decrementGreenSourceErrorPlan.isPlanExecutable();

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Test is plan executable for no available green sources")
	void testIsPlanExecutableForNoGreenSources() {
		// given
		var testAdaptationGoal = new AdaptationGoal(2, "MINIMIZE_USED_BACKUP_POWER", 0.7, true, 0.3);

		doReturn(testAdaptationGoal).when(mockMonitoring).getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER);
		doReturn(List.of("test_server1", "test_server2")).when(mockMonitoring).getAliveAgents(SERVER);
		doReturn(prepareServerMonitoringData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(SERVER_MONITORING, List.of("test_server1", "test_server2"),
						MONITOR_SYSTEM_DATA_TIME_PERIOD);
		doReturn(emptyList()).when(mockMonitoring).getAliveAgents(GREEN_SOURCE);

		// when
		var result = decrementGreenSourceErrorPlan.isPlanExecutable();

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Test is plan executable for success")
	void testIsPlanExecutableForSuccess() {
		// given
		var testAdaptationGoal = new AdaptationGoal(2, "MINIMIZE_USED_BACKUP_POWER", 0.7, true, 0.3);
		var data1 = new WeatherShortages(1, 2000);
		var data3 = new WeatherShortages(3, 2000);
		var data4 = new WeatherShortages(1, 2000);

		var monitoringData1 = List.of(new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, data1));
		var monitoringData2 = List.of(
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, data3),
				new AgentData(now(), "test_gs4", WEATHER_SHORTAGES, data4)
		);

		doReturn(testAdaptationGoal).when(mockMonitoring).getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER);
		doReturn(List.of("test_server1", "test_server2")).when(mockMonitoring).getAliveAgents(SERVER);
		doReturn(prepareServerMonitoringData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(SERVER_MONITORING), eq(List.of("test_server1", "test_server2")),
						anyDouble());
		doReturn(monitoringData1).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), eq(List.of("test_gs1")),
						anyDouble());
		doReturn(monitoringData2).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), eq(List.of("test_gs3", "test_gs4")),
						anyDouble());

		// when
		var result = decrementGreenSourceErrorPlan.isPlanExecutable();
		var greenSourceMap = decrementGreenSourceErrorPlan.getGreenSourcesPerServers();

		// then
		var expectedContent = Map.of(
				new AgentsBackUpPower("test_server1", 0.75),
				List.of(new AgentsPowerShortages("test_gs1", 1))
		);

		assertThat(result).isTrue();
		assertThat(greenSourceMap)
				.as("Result has correct size equal to 1")
				.hasSize(1)
				.as("Result contains correct content")
				.containsExactlyInAnyOrderEntriesOf(expectedContent);
	}

	@Test
	@DisplayName("Test construct adaptation plan for empty green source map")
	void testConstructAdaptationPlanForEmptyMap() {
		assertThat(decrementGreenSourceErrorPlan.constructAdaptationPlan()).isNull();
	}

	@Test
	@DisplayName("Test constructing the adaptation plan")
	void testConstructAdaptationPlan() {
		// given
		var greenSourceMap = Map.of(
				new AgentsBackUpPower("test_server1", 0.75),
				List.of(new AgentsPowerShortages("test_gs1", 1),
						new AgentsPowerShortages("test_gs2", 0)),
				new AgentsBackUpPower("test_server2", 0.7),
				List.of(new AgentsPowerShortages("test_gs3", 1),
						new AgentsPowerShortages("test_gs4", 0))
		);
		decrementGreenSourceErrorPlan.setGreenSourcesPerServers(greenSourceMap);

		// when
		var result = decrementGreenSourceErrorPlan.constructAdaptationPlan();

		// then
		assertThat(result).isNotNull();
		assertThat(result.targetAgent)
				.matches(aid -> ((AID) aid).getName().equals("test_gs2"));
		assertThat(result.getActionParameters())
				.isNotNull()
				.isInstanceOfSatisfying(AdjustGreenSourceErrorParameters.class, params -> {
					assertThat(params.dependsOnOtherAgents()).isFalse();
					assertThat(params.getPercentageChange()).isEqualTo(-0.02);
				});
	}

	private void prepareNetworkStructure() {
		doReturn(List.of("test_server1", "test_server2")).when(mockScenarioStructure)
				.getServersForCloudNetworkAgent("test_cna1");
		doReturn(List.of("test_gs1", "test_gs2")).when(mockScenarioStructure)
				.getGreenSourcesForServerAgent("test_server1");
		doReturn(List.of("test_gs3", "test_gs4")).when(mockScenarioStructure)
				.getGreenSourcesForServerAgent("test_server2");
		doReturn(List.of("test_gs1", "test_gs2", "test_gs3", "test_gs4")).when(mockScenarioStructure)
				.getGreenSourcesForCloudNetwork("test_cna1");
		doReturn(List.of(
				ImmutableCloudNetworkArgs.builder().name("test_cna1").build()
		)).when(mockScenarioStructure).getCloudNetworkAgentsArgs();
		doReturn(mockScenarioStructure).when(mockManagingAgent).getGreenCloudStructure();
	}

	private List<AgentData> prepareGreenSourceWeatherShortageData() {
		var data1 = new WeatherShortages(1, 2000);
		var data2 = new WeatherShortages(1, 2000);
		var data3 = new WeatherShortages(3, 2000);
		var data4 = new WeatherShortages(1, 2000);
		var data5 = new WeatherShortages(1, 2000);

		return List.of(
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, data1),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, data2),
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, data3),
				new AgentData(now(), "test_gs4", WEATHER_SHORTAGES, data4),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, data5)
		);
	}

	private List<AgentData> prepareGreenSourceMonitoringData() {
		var data1 = ImmutableGreenSourceMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.weatherPredictionError(0.04)
				.isBeingDisconnected(false)
				.build();
		var data2 = ImmutableGreenSourceMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(false)
				.build();
		var data3 = ImmutableGreenSourceMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.weatherPredictionError(0.06)
				.isBeingDisconnected(false)
				.build();
		var data4 = ImmutableGreenSourceMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.weatherPredictionError(0.08)
				.isBeingDisconnected(false)
				.build();

		return List.of(
				new AgentData(now(), "test_gs1", GREEN_SOURCE_MONITORING, data1),
				new AgentData(now(), "test_gs2", GREEN_SOURCE_MONITORING, data2),
				new AgentData(now(), "test_gs3", GREEN_SOURCE_MONITORING, data3),
				new AgentData(now(), "test_gs4", GREEN_SOURCE_MONITORING, data4)
		);
	}

	private List<AgentData> prepareServerMonitoringData() {
		var data1 = ImmutableServerMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.availablePower(30D)
				.currentBackUpPowerUsage(0.8)
				.currentMaximumCapacity(100)
				.isDisabled(false)
				.build();
		var data2 = ImmutableServerMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.availablePower(30D)
				.currentBackUpPowerUsage(0.7)
				.currentMaximumCapacity(100)
				.isDisabled(false)
				.build();
		var data3 = ImmutableServerMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.availablePower(30D)
				.currentBackUpPowerUsage(0.8)
				.currentMaximumCapacity(100)
				.isDisabled(false)
				.build();
		var data4 = ImmutableServerMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.availablePower(30D)
				.currentBackUpPowerUsage(0.1)
				.currentMaximumCapacity(100)
				.isDisabled(false)
				.build();

		return List.of(
				new AgentData(now(), "test_server1", SERVER_MONITORING, data1),
				new AgentData(now(), "test_server1", SERVER_MONITORING, data2),
				new AgentData(now(), "test_server2", SERVER_MONITORING, data3),
				new AgentData(now(), "test_server2", SERVER_MONITORING, data4)
		);
	}
}
