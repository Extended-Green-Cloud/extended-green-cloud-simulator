package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static java.time.Instant.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;
import static org.greencloud.commons.args.agent.AgentType.SERVER;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.SOLAR;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceConnectionParameters;
import org.greencloud.commons.args.agent.greenenergy.factory.ImmutableGreenEnergyArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class DisconnectGreenSourcePlanUnitTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ScenarioStructureArgs mockStructure;
	@Mock
	private DisconnectGreenSourcePlan disconnectGreenSourcePlan;
	@Mock
	private MonitoringService mockMonitoring;
	@Mock
	private TimescaleDatabase mockDatabase;

	@BeforeEach
	void init() {
		var mockAgentNode = mock(ManagingAgentNode.class);
		mockManagingAgent = spy(ManagingAgent.class);
		mockStructure = mock(ScenarioStructureArgs.class);
		mockMonitoring = spy(new MonitoringService(mockManagingAgent));
		mockDatabase = spy(TimescaleDatabase.setUpForTests());

		doReturn(mockStructure).when(mockManagingAgent).getGreenCloudStructure();
		doReturn(mockMonitoring).when(mockManagingAgent).monitor();
		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(mockDatabase).when(mockAgentNode).getDatabaseClient();
		disconnectGreenSourcePlan = spy(new DisconnectGreenSourcePlan(mockManagingAgent, DISTRIBUTE_TRAFFIC_EVENLY));

		prepareNetworkStructure();
		prepareMockGreenSourceData();
		prepareMockServerData();
	}

	@Test
	@DisplayName("Test get green sources for disconnection for no alive agents")
	void testGetGreenSourcesForDisconnectionNoAlive() {
		var greenSourcesWithServers = Map.of(
				"test_gs1", List.of("test_server1", "test_server2"),
				"test_gs2", List.of("test_server1", "test_server3"),
				"test_gs3", List.of("test_server3", "test_server3")
		);

		doReturn(prepareNotAliveGreenSourcesData()).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		assertThat(disconnectGreenSourcePlan.getGreenSourcesForDisconnection(greenSourcesWithServers)).isEmpty();
	}

	@Test
	@DisplayName("Test get green sources for disconnection")
	void testGetGreenSourcesForDisconnection() {
		var greenSourcesWithServers = Map.of(
				"test_gs1", List.of("test_server1", "test_server2"),
				"test_gs2", List.of("test_server1", "test_server3"),
				"test_gs3", List.of("test_server3")
		);

		doReturn(prepareAliveGreenSourcesData()).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		var expectedResult = Map.of(
				"test_gs1@192.168.56.1:6996/JADE", List.of("test_server1", "test_server2"),
				"test_gs2@192.168.56.1:6996/JADE", List.of("test_server1", "test_server3")
		);

		assertThat(disconnectGreenSourcePlan.getGreenSourcesForDisconnection(greenSourcesWithServers))
				.as("Result has correct size equal to 2")
				.hasSize(2)
				.as("Result contains correct green sources")
				.containsExactlyInAnyOrderEntriesOf(expectedResult);
	}

	@Test
	@DisplayName("Test get green source with servers for no green sources for disconnection")
	void testGetGreenSourcesWithServersForDisconnection() {
		var greenSourcesWithServers = Map.of(
				"test_gs1@192.168.56.1:6996/JADE", List.of("test_server1", "test_server2"),
				"test_gs2@192.168.56.1:6996/JADE", List.of("test_server2", "test_server3")
		);

		var aliveAgents = new ArrayList<>(prepareAliveGreenSourcesData());
		aliveAgents.addAll(prepareAliveServersData());

		doReturn(aliveAgents).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		var expectedMap = Map.of(
				"test_gs1@192.168.56.1:6996/JADE", List.of("test_server2@192.168.56.1:6996/JADE"),
				"test_gs2@192.168.56.1:6996/JADE",
				List.of("test_server2@192.168.56.1:6996/JADE", "test_server3@192.168.56.1:6996/JADE")
		);

		assertThat(disconnectGreenSourcePlan.getGreenSourcesWithServersForDisconnection(greenSourcesWithServers))
				.as("Result has correct size equal to 2")
				.hasSize(2)
				.as("Result contains correct fields")
				.containsExactlyInAnyOrderEntriesOf(expectedMap);
	}

	@Test
	@DisplayName("Test is plan executable for no green sources with connected servers")
	void isPlanExecutableNotEnoughConnectedServers() {
		var mockGS1 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server2")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS2 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring2")
				.ownerSever("test_server2")
				.connectedServers(new ArrayList<>(List.of("test_server1")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS3 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server2")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		doReturn(List.of(mockGS1, mockGS2, mockGS3)).when(mockStructure).getGreenEnergyAgentsArgs();

		var result = disconnectGreenSourcePlan.isPlanExecutable();

		verify(disconnectGreenSourcePlan, times(0)).getGreenSourcesForDisconnection(anyMap());

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Test is plan executable for no green sources valid for disconnection")
	void isPlanExecutableNoGreenSourcesValidForDisconnection() {
		doReturn(prepareNotAliveGreenSourcesData()).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		var expectedGreenSourcesCall = Map.of(
				"test_gs1", List.of("test_server1", "test_server2"),
				"test_gs2", List.of("test_server2", "test_server3")
		);

		var result = disconnectGreenSourcePlan.isPlanExecutable();

		verify(disconnectGreenSourcePlan).getGreenSourcesForDisconnection(expectedGreenSourcesCall);
		verify(disconnectGreenSourcePlan, times(0)).getGreenSourcesWithServersForDisconnection(anyMap());

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Test is plan executable for no servers valid for disconnection")
	void isPlanExecutableNoServersValidForDisconnection() {
		var mockGS1 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server2", "test_server1")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS2 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring2")
				.ownerSever("test_server2")
				.connectedServers(new ArrayList<>(List.of("test_server3", "test_server5")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS3 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server4")
				.connectedServers(new ArrayList<>(List.of("test_server4")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		doReturn(List.of(mockGS1, mockGS2, mockGS3)).when(mockStructure).getGreenEnergyAgentsArgs();

		var aliveAgents = new ArrayList<>(prepareAliveGreenSourcesData());
		aliveAgents.addAll(prepareAliveServersData());

		doReturn(aliveAgents).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		var expectedGreenSourcesWithServers = Map.of(
				"test_gs1", List.of("test_server2", "test_server1"),
				"test_gs2", List.of("test_server3", "test_server5")
		);

		var expectedGreenSourcesCall = Map.of(
				"test_gs1@192.168.56.1:6996/JADE", List.of("test_server2", "test_server1"),
				"test_gs2@192.168.56.1:6996/JADE", List.of("test_server3", "test_server5")
		);

		var result = disconnectGreenSourcePlan.isPlanExecutable();

		verify(disconnectGreenSourcePlan).getGreenSourcesForDisconnection(expectedGreenSourcesWithServers);
		verify(disconnectGreenSourcePlan).getGreenSourcesWithServersForDisconnection(expectedGreenSourcesCall);

		assertThat(result).isFalse();

	}

	@Test
	@DisplayName("Test is plan executable")
	void isPlanExecutable() {
		var aliveAgents = new ArrayList<>(prepareAliveGreenSourcesData());
		aliveAgents.addAll(prepareAliveServersData());

		doReturn(aliveAgents).when(mockDatabase)
				.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK), MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		var expectedGreenSourcesWithServers = Map.of(
				"test_gs1", List.of("test_server1", "test_server2"),
				"test_gs2", List.of("test_server2", "test_server3")
		);

		var expectedGreenSourcesCall = Map.of(
				"test_gs1@192.168.56.1:6996/JADE", List.of("test_server1", "test_server2"),
				"test_gs2@192.168.56.1:6996/JADE", List.of("test_server2", "test_server3")
		);

		var result = disconnectGreenSourcePlan.isPlanExecutable();

		verify(disconnectGreenSourcePlan).getGreenSourcesForDisconnection(expectedGreenSourcesWithServers);
		verify(disconnectGreenSourcePlan).getGreenSourcesWithServersForDisconnection(expectedGreenSourcesCall);

		assertThat(disconnectGreenSourcePlan.getGreenSourcesWithServers())
				.as("Result has correct size equal to 2")
				.hasSize(2)
				.as("Result contains correct fields")
				.containsExactlyInAnyOrderEntriesOf(Map.of(
						"test_gs1@192.168.56.1:6996/JADE", List.of("test_server2@192.168.56.1:6996/JADE"),
						"test_gs2@192.168.56.1:6996/JADE",
						List.of("test_server2@192.168.56.1:6996/JADE", "test_server3@192.168.56.1:6996/JADE")
				));
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Test construct adaptation plan for no green sources with servers")
	void testConstructAdaptationPlanEmptyMap() {
		assertThat(disconnectGreenSourcePlan.constructAdaptationPlan()).isNull();
	}

	@Test
	@Disabled
	@DisplayName("Test construct adaptation plan")
	void testConstructAdaptationPlan() {
		var testMap = Map.of(
				"test_gs1@192.168.56.1:6996/JADE", List.of("test_server2"),
				"test_gs2@192.168.56.1:6996/JADE", List.of("test_server2", "test_server3")
		);
		disconnectGreenSourcePlan.setGreenSourcesWithServers(testMap);

		var result = disconnectGreenSourcePlan.constructAdaptationPlan();

		assertThat(result.getTargetAgent().getName()).isEqualTo("test_gs2@192.168.56.1:6996/JADE");
		assertThat(result.getActionParameters())
				.isInstanceOfSatisfying(ChangeGreenSourceConnectionParameters.class,
						params -> assertThat(params.getServerName()).isEqualTo("test_server2"));
	}

	private List<AgentData> prepareAliveGreenSourcesData() {
		return List.of(
				new AgentData(now(), "test_gs1@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, GREEN_ENERGY)),
				new AgentData(now(), "test_gs2@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, GREEN_ENERGY)),
				new AgentData(now(), "test_gs3@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, GREEN_ENERGY))
		);
	}

	private List<AgentData> prepareAliveServersData() {
		return List.of(
				new AgentData(now(), "test_server1@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, SERVER)),
				new AgentData(now(), "test_server2@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, SERVER)),
				new AgentData(now(), "test_server3@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, SERVER)),
				new AgentData(now(), "test_server5@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(true, SERVER))
		);
	}

	private List<AgentData> prepareNotAliveGreenSourcesData() {
		return List.of(
				new AgentData(now(), "test_gs1@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(false, GREEN_ENERGY)),
				new AgentData(now(), "test_gs2@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(false, GREEN_ENERGY)),
				new AgentData(now(), "test_gs3@192.168.56.1:6996/JADE", HEALTH_CHECK,
						new HealthCheck(false, GREEN_ENERGY))
		);
	}

	private void prepareNetworkStructure() {
		var mockGS1 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server1", "test_server2")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS2 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server3")
				.connectedServers(new ArrayList<>(List.of("test_server2", "test_server3")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS3 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server3")
				.connectedServers(new ArrayList<>(List.of("test_server3")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();

		doReturn(List.of(mockGS1, mockGS2, mockGS3)).when(mockStructure).getGreenEnergyAgentsArgs();
	}

	void prepareMockGreenSourceData() {
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
		var data4 = ImmutableGreenSourceMonitoringData.builder()
				.successRatio(0.7)
				.currentTraffic(0.6)
				.weatherPredictionError(0.06)
				.isBeingDisconnected(true)
				.build();

		doReturn(List.of(
				new AgentData(now(), "test_gs1@192.168.56.1:6996/JADE", GREEN_SOURCE_MONITORING, data1),
				new AgentData(now(), "test_gs2@192.168.56.1:6996/JADE", GREEN_SOURCE_MONITORING, data2),
				new AgentData(now(), "test_gs3@192.168.56.1:6996/JADE", GREEN_SOURCE_MONITORING, data4)
		)).when(mockDatabase).readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING));

		doReturn(List.of(
				new AgentData(now(), "test_gs1@192.168.56.1:6996/JADE", GREEN_SOURCE_MONITORING, data1),
				new AgentData(now(), "test_gs2@192.168.56.1:6996/JADE", GREEN_SOURCE_MONITORING, data2),
				new AgentData(now(), "test_gs2@192.168.56.1:6996/JADE", GREEN_SOURCE_MONITORING, data3)
		)).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(GREEN_SOURCE_MONITORING), anyList(), anyDouble());
	}

	void prepareMockServerData() {
		var data1 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.7)
				.successRatio(0.8)
				.isDisabled(false)
				.serverJobs(10)
				.currentBackUpPowerTraffic(0.7)
				.currentPowerConsumption(0.8)
				.idlePowerConsumption(20)
				.build();
		var data2 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.4)
				.successRatio(0.8)
				.isDisabled(false)
				.serverJobs(10)
				.currentBackUpPowerTraffic(0.7)
				.currentPowerConsumption(0.8)
				.idlePowerConsumption(20)
				.build();
		var data3 = ImmutableServerMonitoringData.builder()
				.currentTraffic(0.8)
				.successRatio(0.8)
				.isDisabled(false)
				.serverJobs(10)
				.currentBackUpPowerTraffic(0.7)
				.currentPowerConsumption(0.8)
				.idlePowerConsumption(20)
				.build();

		var mockData = List.of(
				new AgentData(now(), "test_server2", SERVER_MONITORING, data1),
				new AgentData(now(), "test_server2", SERVER_MONITORING, data2),
				new AgentData(now(), "test_server3", SERVER_MONITORING, data3)
		);

		doReturn(mockData).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(SERVER_MONITORING),
						anyList(),
						eq(MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD));
	}
}
