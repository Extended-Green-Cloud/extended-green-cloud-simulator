package com.greencloud.commons.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.commons.args.agent.cloudnetwork.ImmutableCloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;

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

	private void prepareScenarioStructure() {
		var mockGS1 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs1")
				.monitoringAgent("test_monitoring1")
				.ownerSever("test_server1")
				.latitude("50")
				.longitude("30")
				.pricePerPowerUnit("10")
				.weatherPredictionError("0.02")
				.maximumCapacity("150")
				.energyType("SOLAR")
				.build();
		var mockGS2 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring2")
				.ownerSever("test_server1")
				.latitude("10")
				.longitude("20")
				.pricePerPowerUnit("20")
				.weatherPredictionError("0.02")
				.maximumCapacity("100")
				.energyType("WIND")
				.build();
		var mockGS3 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring3")
				.ownerSever("test_server2")
				.latitude("15")
				.longitude("50")
				.pricePerPowerUnit("300")
				.weatherPredictionError("0.02")
				.maximumCapacity("500")
				.energyType("SOLAR")
				.build();
		var mockGS4 = ImmutableGreenEnergyAgentArgs.builder()
				.name("test_gs4")
				.monitoringAgent("test_monitoring4")
				.ownerSever("test_server3")
				.latitude("1")
				.longitude("5")
				.pricePerPowerUnit("5")
				.weatherPredictionError("0.02")
				.maximumCapacity("50")
				.energyType("WIND")
				.build();

		var mockServer1 = ImmutableServerAgentArgs.builder()
				.name("test_server1")
				.ownerCloudNetwork("test_cna1")
				.maximumCapacity("100")
				.jobProcessingLimit("2")
				.price("100")
				.build();
		var mockServer2 = ImmutableServerAgentArgs.builder()
				.name("test_server2")
				.ownerCloudNetwork("test_cna1")
				.maximumCapacity("300")
				.jobProcessingLimit("10")
				.price("50")
				.build();
		var mockServer3 = ImmutableServerAgentArgs.builder()
				.name("test_server3")
				.ownerCloudNetwork("test_cna2")
				.maximumCapacity("150")
				.jobProcessingLimit("2")
				.price("200")
				.build();

		var mockCNA1 = ImmutableCloudNetworkArgs.builder()
				.name("test_cna1")
				.build();
		var mockCNA2 = ImmutableCloudNetworkArgs.builder()
				.name("test_cna2")
				.build();

		scenarioStructureArgs = new ScenarioStructureArgs(
				List.of(mockCNA1, mockCNA2),
				List.of(mockServer1, mockServer2, mockServer3),
				List.of(mockGS1, mockGS2, mockGS3, mockGS4)
		);

	}
}
