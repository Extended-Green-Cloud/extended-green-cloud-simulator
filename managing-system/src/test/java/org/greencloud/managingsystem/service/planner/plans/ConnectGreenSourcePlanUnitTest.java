package org.greencloud.managingsystem.service.planner.plans;

import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
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

import com.greencloud.commons.args.agent.cloudnetwork.ImmutableCloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceConnectionParameters;
import com.greencloud.commons.scenario.ScenarioStructureArgs;

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
		connectGreenSourcePlan = spy(new ConnectGreenSourcePlan(mockManagingAgent));
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
				"test_cna1", Map.of("test_server1@192.168.56.1:6996/JADE", 0.7),
				"test_cna2", Map.of("test_server2@192.168.56.1:6996/JADE", 0.5)
		)).when(connectGreenSourcePlan).getAvailableServersMap();
		doReturn(Collections.emptyMap()).when(connectGreenSourcePlan).getAvailableGreenSourcesMap();

		assertThat(connectGreenSourcePlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test verifying if the plan is executable for no available connections")
	void testIsPlanExecutableNoConnections() {
		doReturn(Map.of(
				"test_cna1", Map.of("test_server1@192.168.56.1:6996/JADE", 0.7),
				"test_cna2", Map.of("test_server2@192.168.56.1:6996/JADE", 0.5)
		)).when(connectGreenSourcePlan).getAvailableServersMap();
		doReturn(Map.of(
				"test_cna1", Map.of("test_gs1@192.168.56.1:6996/JADE", 0.3),
				"test_cna2", Map.of("test_gs2@192.168.56.1:6996/JADE", 0.4)
		)).when(connectGreenSourcePlan).getAvailableGreenSourcesMap();
		doReturn(Collections.emptyMap()).when(connectGreenSourcePlan)
				.getConnectableServersForGreenSources(anyMap(), anyMap());

		assertThat(connectGreenSourcePlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test verifying if the plan is executable for available connections")
	void testIsPlanExecutableSuccess() {
		doReturn(Map.of(
				"test_cna1", Map.of("test_server1@192.168.56.1:6996/JADE", 0.7),
				"test_cna2", Map.of("test_server2@192.168.56.1:6996/JADE", 0.5)
		)).when(connectGreenSourcePlan).getAvailableServersMap();
		doReturn(Map.of(
				"test_cna1", Map.of("test_gs1@192.168.56.1:6996/JADE", 0.3),
				"test_cna2", Map.of("test_gs2@192.168.56.1:6996/JADE", 0.4)
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
	@DisplayName("Test getting servers for cloud network when no servers are sufficient")
	void testGetServersForCNAEmptySet() {
		var testServerList = List.of("test_server1", "test_server2");
		var aliveAgents = List.of("test_server1@192.168.56.1:6996/JADE", "test_server2@192.168.56.1:6996/JADE");

		doReturn(testServerList).when(mockStructure).getServersForCloudNetworkAgent("test_cna");
		doReturn(Map.of(
				"test_server1@192.168.56.1:6996/JADE", 0.95,
				"test_server2@192.168.56.1:6996/JADE", 0.98
		)).when(connectGreenSourcePlan).getAverageTrafficForServers(aliveAgents);

		assertThat(connectGreenSourcePlan.getServersForCNA("test_cna", aliveAgents)).isEmpty();
	}

	@Test
	@DisplayName("Test getting servers for cloud network when no servers are alive")
	void testGetServersForCNANoServersAlive() {
		var testServerList = List.of("test_server1", "test_server2");

		doReturn(testServerList).when(mockStructure).getServersForCloudNetworkAgent("test_cna");
		doReturn(Collections.emptyMap()).when(connectGreenSourcePlan)
				.getAverageTrafficForServers(Collections.emptyList());

		assertThat(connectGreenSourcePlan.getServersForCNA("test_cna", Collections.emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Test getting servers for cloud network when there are some servers")
	void testGetServersForCNAAvailableServers() {
		var testServerList = List.of("test_server1", "test_server2");
		var aliveAgents = List.of("test_server1@192.168.56.1:6996/JADE", "test_server2@192.168.56.1:6996/JADE");

		doReturn(testServerList).when(mockStructure).getServersForCloudNetworkAgent("test_cna");
		doReturn(Map.of(
				"test_server1@192.168.56.1:6996/JADE", 0.7,
				"test_server2@192.168.56.1:6996/JADE", 0.98
		)).when(connectGreenSourcePlan).getAverageTrafficForServers(aliveAgents);

		assertThat(connectGreenSourcePlan.getServersForCNA("test_cna", aliveAgents))
				.hasSize(1)
				.allMatch(data -> data.name().equals("test_server1@192.168.56.1:6996/JADE") && data.value() == 0.7);
	}

	@Test
	@DisplayName("Test getting green sources for cloud network when green sources have not enough power")
	void testGetGreenSourcesForCNANotEnoughPower() {
		var testGreenSourceList = List.of("test_gs1", "test_gs2", "test_gs3");
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE"
		);

		doReturn(testGreenSourceList).when(mockStructure).getGreenSourcesForCloudNetwork("test_cna");
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.6,
				"test_gs2@192.168.56.1:6996/JADE", 0.5,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(aliveAgents);

		assertThat(connectGreenSourcePlan.getGreenSourcesForCNA("test_cna", aliveAgents)).isEmpty();
	}

	@Test
	@DisplayName("Test getting green sources for cloud network when green sources have invalid traffic")
	void testGetGreenSourcesForCNAInvalidTraffic() {
		var testGreenSourceList = List.of("test_gs1", "test_gs2", "test_gs3");
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE"
		);

		doReturn(testGreenSourceList).when(mockStructure).getGreenSourcesForCloudNetwork("test_cna");
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.7,
				"test_gs2@192.168.56.1:6996/JADE", 0.8,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(aliveAgents);
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.6,
				"test_gs2@192.168.56.1:6996/JADE", 0.9
		)).when(connectGreenSourcePlan)
				.getAverageTrafficForSources(argThat(t -> t.containsAll(List.of("test_gs1@192.168.56.1:6996/JADE",
						"test_gs2@192.168.56.1:6996/JADE"))));

		assertThat(connectGreenSourcePlan.getGreenSourcesForCNA("test_cna", aliveAgents)).isEmpty();
	}

	@Test
	@DisplayName("Test getting green sources for cloud network when green sources are available")
	void testGetGreenSourcesForCNA() {
		var testGreenSourceList = List.of("test_gs1", "test_gs2", "test_gs3");
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE"
		);

		doReturn(testGreenSourceList).when(mockStructure).getGreenSourcesForCloudNetwork("test_cna");
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.7,
				"test_gs2@192.168.56.1:6996/JADE", 0.8,
				"test_gs3@192.168.56.1:6996/JADE", 0.55
		)).when(connectGreenSourcePlan).getAveragePowerForSources(aliveAgents);
		doReturn(Map.of(
				"test_gs1@192.168.56.1:6996/JADE", 0.4,
				"test_gs2@192.168.56.1:6996/JADE", 0.9
		)).when(connectGreenSourcePlan)
				.getAverageTrafficForSources(argThat(t -> t.containsAll(List.of("test_gs1@192.168.56.1:6996/JADE",
						"test_gs2@192.168.56.1:6996/JADE"))));

		assertThat(connectGreenSourcePlan.getGreenSourcesForCNA("test_cna", aliveAgents))
				.hasSize(1)
				.matches(data -> data.stream()
						.allMatch(greenSource -> greenSource.name().equals("test_gs1@192.168.56.1:6996/JADE")
								&& greenSource.value().equals(0.4)));
	}

	@Test
	@DisplayName("Test getting available servers map for cloud networks")
	void testGetAvailableServersMap() {
		var aliveAgents = List.of(
				"test_server1@192.168.56.1:6996/JADE",
				"test_server2@192.168.56.1:6996/JADE",
				"test_server3@192.168.56.1:6996/JADE"
		);

		doReturn(List.of(
				ImmutableCloudNetworkArgs.builder().name("test_cna1").build(),
				ImmutableCloudNetworkArgs.builder().name("test_cna2").build())
		).when(mockStructure).getCloudNetworkAgentsArgs();
		doReturn(aliveAgents).when(mockMonitoring).getAliveAgents(SERVER);

		doReturn(List.of("test_server1", "test_server2"))
				.when(mockStructure).getServersForCloudNetworkAgent("test_cna1");
		doReturn(List.of("test_server3"))
				.when(mockStructure).getServersForCloudNetworkAgent("test_cna2");

		doReturn(Map.of(
				"test_server1@192.168.56.1:6996/JADE", 0.7,
				"test_server2@192.168.56.1:6996/JADE", 0.98
		)).when(connectGreenSourcePlan).getAverageTrafficForServers(List.of(
				"test_server1@192.168.56.1:6996/JADE",
				"test_server2@192.168.56.1:6996/JADE"));
		doReturn(Map.of(
				"test_server3@192.168.56.1:6996/JADE", 0.6
		)).when(connectGreenSourcePlan).getAverageTrafficForServers(List.of("test_server3@192.168.56.1:6996/JADE"));

		var expectedMap = Map.of(
				"test_cna1", List.of(new AgentsTraffic("test_server1@192.168.56.1:6996/JADE", 0.7)),
				"test_cna2", List.of(new AgentsTraffic("test_server3@192.168.56.1:6996/JADE", 0.6))
		);

		assertThat(connectGreenSourcePlan.getAvailableServersMap())
				.hasSize(2)
				.satisfies(data ->
						assertThat(data.entrySet()).containsExactlyInAnyOrderElementsOf(expectedMap.entrySet()));
	}

	@Test
	@DisplayName("Test getting available green sources map for cloud networks")
	void testGetAvailableGreenSourcesMap() {
		var aliveAgents = List.of(
				"test_gs1@192.168.56.1:6996/JADE",
				"test_gs2@192.168.56.1:6996/JADE",
				"test_gs3@192.168.56.1:6996/JADE",
				"test_gs4@192.168.56.1:6996/JADE"
		);

		doReturn(List.of(
				ImmutableCloudNetworkArgs.builder().name("test_cna1").build(),
				ImmutableCloudNetworkArgs.builder().name("test_cna2").build())
		).when(mockStructure).getCloudNetworkAgentsArgs();
		doReturn(aliveAgents).when(mockMonitoring).getAliveAgents(GREEN_SOURCE);

		doReturn(List.of("test_gs1", "test_gs2", "test_gs3")).when(mockStructure)
				.getGreenSourcesForCloudNetwork("test_cna1");
		doReturn(List.of("test_gs4")).when(mockStructure).getGreenSourcesForCloudNetwork("test_cna2");

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
		)).when(connectGreenSourcePlan)
				.getAverageTrafficForSources(argThat(t -> t.containsAll(List.of("test_gs1@192.168.56.1:6996/JADE",
						"test_gs2@192.168.56.1:6996/JADE"))));
		doReturn(Map.of(
				"test_gs4@192.168.56.1:6996/JADE", 0.3
		)).when(connectGreenSourcePlan).getAverageTrafficForSources(List.of("test_gs4@192.168.56.1:6996/JADE"));

		var expectedMap = Map.of(
				"test_cna1", Set.of(new AgentsGreenPower("test_gs2@192.168.56.1:6996/JADE", 0.4)),
				"test_cna2", Set.of(new AgentsGreenPower("test_gs4@192.168.56.1:6996/JADE", 0.3))
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

		var serversForCloudNetworks = Map.of(
				"test_cna1", List.of(
						new AgentsTraffic("test_server1@192.168.56.1:6996/JADE", 0.4),
						new AgentsTraffic("test_server2@192.168.56.1:6996/JADE", 0.6)),
				"test_cna2", List.of(
						new AgentsTraffic("test_server3@192.168.56.1:6996/JADE", 0.5),
						new AgentsTraffic("test_server4@192.168.56.1:6996/JADE", 0.65))
		);
		var greenSourcesForCloudNetworks = Map.of(
				"test_cna1", Set.of(
						new AgentsGreenPower("test_gs1@192.168.56.1:6996/JADE", 0.4),
						new AgentsGreenPower("test_gs2@192.168.56.1:6996/JADE", 0.5),
						new AgentsGreenPower("test_gs3@192.168.56.1:6996/JADE", 0.65)),
				"test_cna2", Set.of(
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
				connectGreenSourcePlan.getConnectableServersForGreenSources(serversForCloudNetworks,
						greenSourcesForCloudNetworks);

		assertThat(result)
				.hasSize(6)
				.containsExactlyInAnyOrderEntriesOf(expectedResult);
	}

	private void prepareNetworkStructure() {
		var mockGS1 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server1")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();
		var mockGS2 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server1")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();
		var mockGS3 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server2")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();
		var mockGS4 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs4")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server3")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();
		var mockGS5 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs5")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server3")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();
		var mockGS6 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs6")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.connectedServers(new ArrayList<>(List.of("test_server4")))
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();

		doReturn(List.of(mockGS1, mockGS2, mockGS3, mockGS4, mockGS5, mockGS6)).when(mockStructure)
				.getGreenEnergyAgentsArgs();
	}

}
