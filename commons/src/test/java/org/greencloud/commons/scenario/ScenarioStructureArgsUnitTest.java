package org.greencloud.commons.scenario;

import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.SOLAR;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.greencloud.commons.args.agent.cloudnetwork.factory.ImmutableCloudNetworkArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.ImmutableGreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.ImmutableMonitoringArgs;
import org.greencloud.commons.args.agent.scheduler.factory.ImmutableSchedulerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.managing.ImmutableManagingAgentArgs;
import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;

class ScenarioStructureArgsUnitTest {

	private ScenarioStructureArgs scenarioStructureArgs;

	@BeforeEach
	void init() {
		prepareScenarioStructure();
	}

	@Test
	@DisplayName("Test getting servers for cloud network agent")
	void testGetServersForCloudNetworkAgent() {
		var resultCNA1 = scenarioStructureArgs.getServersForCloudNetworkAgent("test_cna1");
		var resultCNA2 = scenarioStructureArgs.getServersForCloudNetworkAgent("test_cna2");

		assertThat(resultCNA1)
				.as("Result should have size 2")
				.hasSize(2)
				.as("Result should contain correct servers")
				.containsExactlyInAnyOrder("test_server1", "test_server2");
		assertThat(resultCNA2)
				.as("Result should have size 1")
				.hasSize(1)
				.as("Result should contain correct servers")
				.containsExactlyInAnyOrder("test_server3");
	}

	@Test
	@DisplayName("Test getting green sources for server agent")
	void testGetGreenSourcesForServerAgent() {
		var resultServer1 = scenarioStructureArgs.getGreenSourcesForServerAgent("test_server1");
		var resultServer3 = scenarioStructureArgs.getGreenSourcesForServerAgent("test_server3");

		assertThat(resultServer1)
				.as("Result should have size 2")
				.hasSize(2)
				.as("Result should contain correct green sources")
				.containsExactlyInAnyOrder("test_gs1", "test_gs2");
		assertThat(resultServer3)
				.as("Result should have size 1")
				.hasSize(1)
				.as("Result should contain correct green sources")
				.containsExactlyInAnyOrder("test_gs4");
	}

	@Test
	@DisplayName("Test getting green sources for cloud network agent")
	void testGetGreenSourcesForCloudNetwork() {
		var resultCNA1 = scenarioStructureArgs.getGreenSourcesForCloudNetwork("test_cna1");
		var resultCNA2 = scenarioStructureArgs.getGreenSourcesForCloudNetwork("test_cna2");

		assertThat(resultCNA1)
				.as("Result should have size 3")
				.hasSize(3)
				.as("Result should contain correct green sources")
				.containsExactlyInAnyOrder("test_gs1", "test_gs2", "test_gs3");
		assertThat(resultCNA2)
				.as("Result should have size 1")
				.hasSize(1)
				.as("Result should contain correct green sources")
				.containsExactlyInAnyOrder("test_gs4");
	}

	@Test
	@DisplayName("Test getting parent CNA for a server")
	void testGetParentCNAForServer() {
		var serverName = "test_server3";
		var expectedCNA = "test_cna2";

		var result = scenarioStructureArgs.getParentCNAForServer(serverName);

		assertThat(result).isEqualTo(expectedCNA);
	}

	@Test
	@DisplayName("Test getting concatenation of agent args for all args present")
	void testGetAgentsArgsAllArgsPresent() {
		var expectedAgents = List.of(
				"test_scheduler",
				"test_managing",
				"test_server1",
				"test_server2",
				"test_server3",
				"test_monitoring1",
				"test_monitoring2",
				"test_monitoring3",
				"test_monitoring4",
				"test_gs1",
				"test_gs2",
				"test_gs3",
				"test_gs4",
				"test_cna1",
				"test_cna2"
		);
		var result = scenarioStructureArgs.getAgentsArgs();

		Assertions.assertThat(result)
				.as("Result has a correct size equal 15")
				.hasSize(15)
				.map(AgentArgs::getName)
				.containsAll(expectedAgents);
	}

	private void prepareScenarioStructure() {
		var mockMonitor1 = ImmutableMonitoringArgs.builder()
				.name("test_monitoring1")
				.badStubProbability(0.02)
				.build();
		var mockMonitor2 = ImmutableMonitoringArgs.builder()
				.name("test_monitoring2")
				.badStubProbability(0.02)
				.build();
		var mockMonitor3 = ImmutableMonitoringArgs.builder()
				.name("test_monitoring3")
				.badStubProbability(0.02)
				.build();
		var mockMonitor4 = ImmutableMonitoringArgs.builder()
				.name("test_monitoring4")
				.badStubProbability(0.02)
				.build();

		var mockGS1 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
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
				.ownerSever("test_server1")
				.latitude("10")
				.longitude("20")
				.pricePerPowerUnit(20L)
				.weatherPredictionError(0.02)
				.maximumCapacity(100L)
				.energyType(WIND)
				.build();
		var mockGS3 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring3")
				.ownerSever("test_server2")
				.latitude("15")
				.longitude("50")
				.pricePerPowerUnit(300L)
				.weatherPredictionError(0.02)
				.maximumCapacity(500L)
				.energyType(SOLAR)
				.build();
		var mockGS4 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs4")
				.monitoringAgent("test_monitoring4")
				.ownerSever("test_server3")
				.latitude("1")
				.longitude("5")
				.pricePerPowerUnit(5L)
				.weatherPredictionError(0.02)
				.maximumCapacity(50L)
				.energyType(WIND)
				.build();

		var mockServer1 = ImmutableServerArgs.builder()
				.name("test_server1")
				.ownerCloudNetwork("test_cna1")
				.jobProcessingLimit(2)
				.price(100D)
				.build();
		var mockServer2 = ImmutableServerArgs.builder()
				.name("test_server2")
				.ownerCloudNetwork("test_cna1")
				.jobProcessingLimit(10)
				.price(50D)
				.build();
		var mockServer3 = ImmutableServerArgs.builder()
				.name("test_server3")
				.ownerCloudNetwork("test_cna2")
				.jobProcessingLimit(2)
				.price(200D)
				.build();

		var mockCNA1 = ImmutableCloudNetworkArgs.builder()
				.name("test_cna1")
				.build();
		var mockCNA2 = ImmutableCloudNetworkArgs.builder()
				.name("test_cna2")
				.build();

		var mockScheduler = ImmutableSchedulerArgs.builder()
				.name("test_scheduler")
				.deadlineWeight(1)
				.cpuWeight(1)
				.maximumQueueSize(10000)
				.build();

		var mockManaging = ImmutableManagingAgentArgs.builder()
				.name("test_managing")
				.systemQualityThreshold(0.8)
				.build();

		scenarioStructureArgs = new ScenarioStructureArgs(
				mockManaging,
				mockScheduler,
				List.of(mockCNA1, mockCNA2),
				List.of(mockServer1, mockServer2, mockServer3),
				List.of(mockMonitor1, mockMonitor2, mockMonitor3, mockMonitor4),
				List.of(mockGS1, mockGS2, mockGS3, mockGS4)
		);

	}
}
