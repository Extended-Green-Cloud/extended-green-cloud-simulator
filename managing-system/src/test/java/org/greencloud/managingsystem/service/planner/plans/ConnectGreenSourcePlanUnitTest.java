package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.types.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.types.DataType.SERVER_MONITORING;
import static com.database.knowledge.types.GoalType.MAXIMIZE_JOB_SUCCESS_RATIO;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.SOLAR;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceConnectionParameters;
import org.greencloud.commons.args.agent.greenenergy.factory.ImmutableGreenEnergyArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.ImmutableRegionalManagerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.domain.location.ImmutableLocation;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsGreenPower;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsTraffic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ConnectGreenSourcePlanUnitTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ScenarioStructureArgs mockStructure;
	@Mock
	private ConnectGreenSourcePlan connectGreenSourcePlan;
	@Mock
	private MonitoringService mockMonitoring;

	@BeforeEach
	void init() {
		mockManagingAgent = spy(ManagingAgent.class);
		mockStructure = mock(ScenarioStructureArgs.class);
		mockMonitoring = spy(new MonitoringService(mockManagingAgent));

		doReturn(mockStructure).when(mockManagingAgent).getGreenCloudStructure();
		doReturn(mockMonitoring).when(mockManagingAgent).monitor();
		connectGreenSourcePlan = spy(new ConnectGreenSourcePlan(mockManagingAgent, MAXIMIZE_JOB_SUCCESS_RATIO));
	}

	@Test
	@DisplayName("Test constructing adaptation plan for invalid data")
	void testConstructAdaptationPlanForInvalidData() {
		assertThat(connectGreenSourcePlan.constructAdaptationPlan()).isNull();
	}

	@Test
	@DisplayName("Test constructing adaptation plan for valid data")
	void testConstructAdaptationPlan() {
		var greenSourceMap = Map.of(
				new AgentsGreenPower("test_gs1@192.168.56.1:6996/JADE", 0.4),
				List.of(new AgentsTraffic("test_server2@192.168.56.1:6996/JADE", 0.6),
						new AgentsTraffic("test_server3@192.168.56.1:6996/JADE", 0.9)),
				new AgentsGreenPower("test_gs2@192.168.56.1:6996/JADE", 0.5),
				List.of(new AgentsTraffic("test_server2@192.168.56.1:6996/JADE", 0.6),
						new AgentsTraffic("test_server5@192.168.56.1:6996/JADE", 0.4))
		);

		connectGreenSourcePlan.setConnectableServersForGreenSource(greenSourceMap);

		assertThat(connectGreenSourcePlan.constructAdaptationPlan())
				.isNotNull()
				.matches(data ->
						data.getTargetAgent().getName().equals("test_gs1@192.168.56.1:6996/JADE") &&
								data.getActionParameters() instanceof ChangeGreenSourceConnectionParameters &&
								((ChangeGreenSourceConnectionParameters) data.getActionParameters()).getServerName()
										.equals("test_server2@192.168.56.1:6996/JADE")
				);
	}

	@Test
	@DisplayName("Test verifying if the plan is executable for no available servers")
	void testIsPlanExecutableNoServers() {
		doReturn(Collections.emptyMap()).when(connectGreenSourcePlan).getAvailableServersMap();

		assertThat(connectGreenSourcePlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test verifying if the plan is executable for no available green sources")
	void testIsPlanExecutableNoGreenSources() {
		doReturn(Map.of(
				"test_rma1", Map.of("test_server1@192.168.56.1:6996/JADE", 0.7),
				"test_rma2", Map.of("test_server2@192.168.56.1:6996/JADE", 0.5)
		)).when(connectGreenSourcePlan).getAvailableServersMap();
		doReturn(Collections.emptyMap()).when(connectGreenSourcePlan).getAvailableGreenSourcesMap();

		assertThat(connectGreenSourcePlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test verifying if the plan is executable for no available connections")
	void testIsPlanExecutableNoConnections() {
		doReturn(Map.of(
				"test_rma1", Map.of("test_server1@192.168.56.1:6996/JADE", 0.7),
				"test_rma2", Map.of("test_server2@192.168.56.1:6996/JADE", 0.5)
		)).when(connectGreenSourcePlan).getAvailableServersMap();
		doReturn(Map.of(
				"test_rma1", Map.of("test_gs1@192.168.56.1:6996/JADE", 0.3),
				"test_rma2", Map.of("test_gs2@192.168.56.1:6996/JADE", 0.4)
		)).when(connectGreenSourcePlan).getAvailableGreenSourcesMap();
		doReturn(Collections.emptyMap()).when(connectGreenSourcePlan)
				.getConnectableServersForGreenSources(anyMap(), anyMap());

		assertThat(connectGreenSourcePlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test verifying if the plan is executable for available connections")
	void testIsPlanExecutableSuccess() {
		doReturn(Map.of(
				"test_rma1", Map.of("test_server1@192.168.56.1:6996/JADE", 0.7),
				"test_rma2", Map.of("test_server2@192.168.56.1:6996/JADE", 0.5)
		)).when(connectGreenSourcePlan).getAvailableServersMap();
		doReturn(Map.of(
				"test_rma1", Map.of("test_gs1@192.168.56.1:6996/JADE", 0.3),
				"test_rma2", Map.of("test_gs2@192.168.56.1:6996/JADE", 0.4)
		)).when(connectGreenSourcePlan).getAvailableGreenSourcesMap();
		doReturn(Map.of(
				new AbstractMap.SimpleEntry<>("test_gs1@192.168.56.1:6996/JADE", 0.4),
				Map.of("test_server2@192.168.56.1:6996/JADE", 0.6),
				new AbstractMap.SimpleEntry<>("test_gs2@192.168.56.1:6996/JADE", 0.5),
				Map.of("test_server2@192.168.56.1:6996/JADE", 0.6)
		)).when(connectGreenSourcePlan).getConnectableServersForGreenSources(anyMap(), anyMap());

		assertThat(connectGreenSourcePlan.isPlanExecutable()).isTrue();
	}

	@Test
	@DisplayName("Test getting servers for regional manager when no servers are sufficient")
	void testGetServersForRMAEmptySet() {
		var testServerList = List.of("test_server1", "test_server2");
		var aliveAgents = List.of("test_server1@192.168.56.1:6996/JADE", "test_server2@192.168.56.1:6996/JADE");

		doReturn(testServerList).when(mockStructure).getServersForRegionalManagerAgent("test_rma");
		doReturn(Map.of(
				"test_server1@192.168.56.1:6996/JADE", 0.95,
				"test_server2@192.168.56.1:6996/JADE", 0.98
		)).when(mockMonitoring).getAverageTrafficForNetworkComponent(aliveAgents, SERVER_MONITORING);

		assertThat(connectGreenSourcePlan.getServersForRMA("test_rma", aliveAgents)).isEmpty();
	}

	@Test
	@DisplayName("Test getting servers for regional manager when no servers are alive")
	void testGetServersForRMANoServersAlive() {
		var testServerList = List.of("test_server1", "test_server2");

		doReturn(testServerList).when(mockStructure).getServersForRegionalManagerAgent("test_rma");
		doReturn(Collections.emptyMap()).when(mockMonitoring)
				.getAverageTrafficForNetworkComponent(emptyList(), SERVER_MONITORING);

		assertThat(connectGreenSourcePlan.getServersForRMA("test_rma", emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Test getting servers for regional manager when there are some servers")
	void testGetServersForRMAAvailableServers() {
		var testServerList = List.of("test_server1", "test_server2");
		var aliveAgents = List.of("test_server1@192.168.56.1:6996/JADE", "test_server2@192.168.56.1:6996/JADE");

		doReturn(testServerList).when(mockStructure).getServersForRegionalManagerAgent("test_rma");
		doReturn(Map.of(
				"test_server1@192.168.56.1:6996/JADE", 0.7,
				"test_server2@192.168.56.1:6996/JADE", 0.98
		)).when(mockMonitoring).getAverageTrafficForNetworkComponent(aliveAgents, SERVER_MONITORING);

		assertThat(connectGreenSourcePlan.getServersForRMA("test_rma", aliveAgents))
				.hasSize(1)
				.allMatch(data -> data.name().equals("test_server1@192.168.56.1:6996/JADE") && data.value() == 0.7);
	}

	@Test
	@DisplayName("Test getting green sources for regional manager when green sources have not enough power")
	void testGetGreenSourcesForRMANotEnoughPower() {
		var testGreenSourceList = List.of("test_gs1", "test_gs2", "test_gs3");
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE"
		);

		doReturn(testGreenSourceList).when(mockStructure).getGreenSourcesForRegionalManager("test_rma");
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.6,
				"test_gs2@192.168.56.1:6996/JADE", 0.5,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(aliveAgents);

		assertThat(connectGreenSourcePlan.getGreenSourcesForRMA("test_rma", aliveAgents)).isEmpty();
	}

	@Test
	@DisplayName("Test getting green sources for regional manager when green sources have invalid traffic")
	void testGetGreenSourcesForRMAInvalidTraffic() {
		var testGreenSourceList = List.of("test_gs1", "test_gs2", "test_gs3");
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE"
		);

		doReturn(testGreenSourceList).when(mockStructure).getGreenSourcesForRegionalManager("test_rma");
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.7,
				"test_gs2@192.168.56.1:6996/JADE", 0.8,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(aliveAgents);
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.6,
				"test_gs2@192.168.56.1:6996/JADE", 0.9
		)).when(mockMonitoring)
				.getAverageTrafficForNetworkComponent(
						argThat(t -> t.containsAll(List.of("test_gs1@192.168.56.1:6996/JADE",
								"test_gs2@192.168.56.1:6996/JADE"))), eq(GREEN_SOURCE_MONITORING));

		assertThat(connectGreenSourcePlan.getGreenSourcesForRMA("test_rma", aliveAgents)).isEmpty();
	}

	@Test
	@DisplayName("Test getting green sources for regional manager when green sources are available")
	void testGetGreenSourcesForRMA() {
		var testGreenSourceList = List.of("test_gs1", "test_gs2", "test_gs3");
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE"
		);

		doReturn(testGreenSourceList).when(mockStructure).getGreenSourcesForRegionalManager("test_rma");
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.7,
				"test_gs2@192.168.56.1:6996/JADE", 0.8,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(aliveAgents);
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.4,
				"test_gs2@192.168.56.1:6996/JADE", 0.9
		)).when(mockMonitoring)
				.getAverageTrafficForNetworkComponent(
						argThat(t -> t.containsAll(List.of("test_gs1@192.168.56.1:6996/JADE",
								"test_gs2@192.168.56.1:6996/JADE"))), eq(GREEN_SOURCE_MONITORING));

		assertThat(connectGreenSourcePlan.getGreenSourcesForRMA("test_rma", aliveAgents))
				.hasSize(1)
				.matches(data -> data.stream()
						.allMatch(greenSource -> greenSource.name().equals("test_gs1@192.168.56.1:6996/JADE")
								&& greenSource.value().equals(0.4)));
	}

	@Test
	@DisplayName("Test getting available servers map for regional managers")
	void testGetAvailableServersMap() {
		var aliveAgents = List.of(
				"test_server1@192.168.56.1:6996/JADE",
				"test_server2@192.168.56.1:6996/JADE",
				"test_server3@192.168.56.1:6996/JADE"
		);

		doReturn(List.of(
				ImmutableRegionalManagerArgs.builder().name("test_rma1").build(),
				ImmutableRegionalManagerArgs.builder().name("test_rma2").build())
		).when(mockStructure).getRegionalManagerAgentsArgs();
		doReturn(aliveAgents).when(mockMonitoring).getAliveAgents(SERVER);

		doReturn(List.of("test_server1", "test_server2"))
				.when(mockStructure).getServersForRegionalManagerAgent("test_rma1");
		doReturn(List.of("test_server3"))
				.when(mockStructure).getServersForRegionalManagerAgent("test_rma2");

		doReturn(Map.of(
				"test_server1@192.168.56.1:6996/JADE", 0.7,
				"test_server2@192.168.56.1:6996/JADE", 0.98
		)).when(mockMonitoring).getAverageTrafficForNetworkComponent(List.of(
				"test_server1@192.168.56.1:6996/JADE",
				"test_server2@192.168.56.1:6996/JADE"), SERVER_MONITORING);
		doReturn(Map.of(
				"test_server3@192.168.56.1:6996/JADE", 0.6
		)).when(mockMonitoring).getAverageTrafficForNetworkComponent(List.of("test_server3@192.168.56.1:6996/JADE"),
				SERVER_MONITORING);
		doReturn(List.of("test_server1@192.168.56.1:6996/JADE", "test_server2@192.168.56.1:6996/JADE",
				"test_server3@192.168.56.1:6996/JADE")).when(mockMonitoring).getActiveServers();

		var expectedMap = Map.of(
				"test_rma1", List.of(new AgentsTraffic("test_server1@192.168.56.1:6996/JADE", 0.7)),
				"test_rma2", List.of(new AgentsTraffic("test_server3@192.168.56.1:6996/JADE", 0.6))
		);

		assertThat(connectGreenSourcePlan.getAvailableServersMap())
				.hasSize(2)
				.satisfies(data ->
						assertThat(data.entrySet()).containsExactlyInAnyOrderElementsOf(expectedMap.entrySet()));
	}

	@Test
	@DisplayName("Test getting available green sources map for regional managers")
	void testGetAvailableGreenSourcesMap() {
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE",
				"test_gs4@192.168.56.1:6996/JADE"
		);

		doReturn(List.of(
				ImmutableRegionalManagerArgs.builder().name("test_rma1").build(),
				ImmutableRegionalManagerArgs.builder().name("test_rma2").build())
		).when(mockStructure).getRegionalManagerAgentsArgs();
		doReturn(aliveAgents).when(mockMonitoring).getAliveAgents(GREEN_ENERGY);

		doReturn(List.of("test_gs1", "test_gs2", "test_gs3")).when(mockStructure)
				.getGreenSourcesForRegionalManager("test_rma1");
		doReturn(List.of("test_gs4")).when(mockStructure).getGreenSourcesForRegionalManager("test_rma2");

		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.9,
				"test_gs2@192.168.56.1:6996/JADE", 0.8,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(List.of("test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE", "test_gs3@192.168.56.1:6996/JADE"));
		doReturn(Map.of("test_gs4@192.168.56.1:6996/JADE", 0.9))
				.when(connectGreenSourcePlan).getAveragePowerForSources(List.of("test_gs4@192.168.56.1:6996/JADE"));

		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.9,
				"test_gs2@192.168.56.1:6996/JADE", 0.4
		)).when(mockMonitoring)
				.getAverageTrafficForNetworkComponent(
						argThat(t -> t.containsAll(List.of("test_gs1@192.168.56.1:6996/JADE",
								"test_gs2@192.168.56.1:6996/JADE"))), eq(GREEN_SOURCE_MONITORING));
		doReturn(Map.of(
				"test_gs4@192.168.56.1:6996/JADE", 0.3
		)).when(mockMonitoring).getAverageTrafficForNetworkComponent(List.of("test_gs4@192.168.56.1:6996/JADE"),
				GREEN_SOURCE_MONITORING);

		var expectedMap = Map.of(
				"test_rma1", Set.of(new AgentsGreenPower("test_gs2@192.168.56.1:6996/JADE", 0.4)),
				"test_rma2", Set.of(new AgentsGreenPower("test_gs4@192.168.56.1:6996/JADE", 0.3))
		);

		assertThat(connectGreenSourcePlan.getAvailableGreenSourcesMap())
				.hasSize(2)
				.satisfies(data ->
						assertThat(data.entrySet()).containsExactlyInAnyOrderElementsOf(expectedMap.entrySet()));
	}

	@Test
	@DisplayName("Test getting connectable servers for green sources")
	void testGetConnectableServersForGreenSources() {
		prepareNetworkStructure();

		var serversForRegionalManagers = Map.of(
				"test_rma1", List.of(
						new AgentsTraffic("test_server1@192.168.56.1:6996/JADE", 0.4),
						new AgentsTraffic("test_server2@192.168.56.1:6996/JADE", 0.6)),
				"test_rma2", List.of(
						new AgentsTraffic("test_server3@192.168.56.1:6996/JADE", 0.5),
						new AgentsTraffic("test_server4@192.168.56.1:6996/JADE", 0.65))
		);
		var greenSourcesForRegionalManagers = Map.of(
				"test_rma1", Set.of(
						new AgentsGreenPower("test_gs1@192.168.56.1:6996/JADE", 0.4),
						new AgentsGreenPower("test_gs2@192.168.56.1:6996/JADE", 0.5),
						new AgentsGreenPower("test_gs3@192.168.56.1:6996/JADE", 0.65)),
				"test_rma2", Set.of(
						new AgentsGreenPower("test_gs4@192.168.56.1:6996/JADE", 0.45),
						new AgentsGreenPower("test_gs5@192.168.56.1:6996/JADE", 0.51),
						new AgentsGreenPower("test_gs6@192.168.56.1:6996/JADE", 0.62))
		);

		var expectedResult = Map.of(
				new AgentsGreenPower("test_gs1@192.168.56.1:6996/JADE", 0.4),
				List.of(new AgentsTraffic("test_server2@192.168.56.1:6996/JADE", 0.6)),
				new AgentsGreenPower("test_gs2@192.168.56.1:6996/JADE", 0.5),
				List.of(new AgentsTraffic("test_server2@192.168.56.1:6996/JADE", 0.6)),
				new AgentsGreenPower("test_gs3@192.168.56.1:6996/JADE", 0.65),
				List.of(new AgentsTraffic("test_server1@192.168.56.1:6996/JADE", 0.4)),
				new AgentsGreenPower("test_gs4@192.168.56.1:6996/JADE", 0.45),
				List.of(new AgentsTraffic("test_server4@192.168.56.1:6996/JADE", 0.65)),
				new AgentsGreenPower("test_gs5@192.168.56.1:6996/JADE", 0.51),
				List.of(new AgentsTraffic("test_server4@192.168.56.1:6996/JADE", 0.65)),
				new AgentsGreenPower("test_gs6@192.168.56.1:6996/JADE", 0.62),
				List.of(new AgentsTraffic("test_server3@192.168.56.1:6996/JADE", 0.5))
		);

		var result =
				connectGreenSourcePlan.getConnectableServersForGreenSources(serversForRegionalManagers,
						greenSourcesForRegionalManagers);

		assertThat(result)
				.hasSize(6)
				.containsExactlyInAnyOrderEntriesOf(expectedResult);
	}

	private void prepareNetworkStructure() {
		var mockGS1 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server1")))
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS2 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server1")))
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
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
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS4 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs4")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server3")))
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS5 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs5")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server3")))
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS6 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs6")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server4")))
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();

		doReturn(List.of(mockGS1, mockGS2, mockGS3, mockGS4, mockGS5, mockGS6)).when(mockStructure)
				.getGreenEnergyAgentsArgs();
	}

}
