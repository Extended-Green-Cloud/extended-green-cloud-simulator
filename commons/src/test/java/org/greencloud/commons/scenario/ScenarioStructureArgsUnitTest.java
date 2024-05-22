package org.greencloud.commons.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.SOLAR;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.greencloud.commons.args.agent.centralmanager.factory.ImmutableCentralManagerArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.ImmutableGreenEnergyArgs;
import org.greencloud.commons.args.agent.managing.ImmutableManagingAgentArgs;
import org.greencloud.commons.args.agent.monitoring.factory.ImmutableMonitoringArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.ImmutableRegionalManagerArgs;
import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.domain.location.ImmutableLocation;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScenarioStructureArgsUnitTest {

	private ScenarioStructureArgs scenarioStructureArgs;

	@BeforeEach
	void init() {
		prepareScenarioStructure();
	}

	@Test
	@DisplayName("Test getting servers for regional manager agent")
	void testGetServersForRegionalManagerAgent() {
		var resultRMA1 = scenarioStructureArgs.getServersForRegionalManagerAgent("test_rma1");
		var resultRMA2 = scenarioStructureArgs.getServersForRegionalManagerAgent("test_rma2");

		assertThat(resultRMA1)
				.as("Result should have size 2")
				.hasSize(2)
				.as("Result should contain correct servers")
				.containsExactlyInAnyOrder("test_server1", "test_server2");
		assertThat(resultRMA2)
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
	@DisplayName("Test getting green sources for regional manager agent")
	void testGetGreenSourcesForRegionalManager() {
		var resultRMA1 = scenarioStructureArgs.getGreenSourcesForRegionalManager("test_rma1");
		var resultRMA2 = scenarioStructureArgs.getGreenSourcesForRegionalManager("test_rma2");

		assertThat(resultRMA1)
				.as("Result should have size 3")
				.hasSize(3)
				.as("Result should contain correct green sources")
				.containsExactlyInAnyOrder("test_gs1", "test_gs2", "test_gs3");
		assertThat(resultRMA2)
				.as("Result should have size 1")
				.hasSize(1)
				.as("Result should contain correct green sources")
				.containsExactlyInAnyOrder("test_gs4");
	}

	@Test
	@DisplayName("Test getting parent RMA for a server")
	void testGetParentRMAForServer() {
		var serverName = "test_server3";
		var expectedRMA = "test_rma2";

		var result = scenarioStructureArgs.getParentRMAForServer(serverName);

		assertThat(result).isEqualTo(expectedRMA);
	}

	@Test
	@DisplayName("Test getting concatenation of agent args for all args present")
	void testGetAgentsArgsAllArgsPresent() {
		var expectedAgents = List.of(
				"test_cma",
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
				"test_rma1",
				"test_rma2"
		);
		var result = scenarioStructureArgs.getAgentsArgs();

		Assertions.assertThat(result)
				.as("Result has a correct size equal 15")
				.hasSize(15)
				.map(AgentArgs::getName)
				.containsAll(expectedAgents);
	}

	private void prepareScenarioStructure() {
		var mockCPUResource = ImmutableResource.builder()
				.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
						.value(20D)
						.unit("cores")
						.resourceCharacteristicAddition("return resource1 + resource2;")
						.resourceCharacteristicReservation("return ownedAmount - amountToReserve;")
						.resourceCharacteristicSubtraction("return ownedAmount - amountToRemove;")
						.build())
				.resourceComparator(
						"import java.lang.Math; return Math.signum(resource1.getAmountInCommonUnit() - resource2.getAmountInCommonUnit());")
				.resourceValidator(
						"requirements.getCharacteristics().containsKey(\"amount\") && resource.getAmountInCommonUnit() >= requirements.getAmountInCommonUnit();")
				.build();

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
				.location(ImmutableLocation.builder().latitude(50).longitude(30).build())
				.pricePerPowerUnit(10L)
				.weatherPredictionError(0.02)
				.maximumCapacity(150L)
				.energyType(SOLAR)
				.build();
		var mockGS2 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs2")
				.monitoringAgent("test_monitoring2")
				.ownerSever("test_server1")
				.location(ImmutableLocation.builder().latitude(10).longitude(20).build())
				.pricePerPowerUnit(20L)
				.weatherPredictionError(0.02)
				.maximumCapacity(100L)
				.energyType(WIND)
				.build();
		var mockGS3 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs3")
				.monitoringAgent("test_monitoring3")
				.ownerSever("test_server2")
				.location(ImmutableLocation.builder().latitude(15).longitude(30).build())
				.pricePerPowerUnit(300L)
				.weatherPredictionError(0.02)
				.maximumCapacity(500L)
				.energyType(SOLAR)
				.build();
		var mockGS4 = ImmutableGreenEnergyArgs.builder()
				.name("test_gs4")
				.monitoringAgent("test_monitoring4")
				.ownerSever("test_server3")
				.location(ImmutableLocation.builder().latitude(1).longitude(5).build())
				.pricePerPowerUnit(5L)
				.weatherPredictionError(0.02)
				.maximumCapacity(50L)
				.energyType(WIND)
				.build();

		var mockServer1 = ImmutableServerArgs.builder()
				.name("test_server1")
				.ownerRegionalManager("test_rma1")
				.jobProcessingLimit(2)
				.price(100D)
				.maxPower(200)
				.idlePower(10)
				.putResources(CPU, mockCPUResource)
				.build();
		var mockServer2 = ImmutableServerArgs.builder()
				.name("test_server2")
				.ownerRegionalManager("test_rma1")
				.jobProcessingLimit(10)
				.price(50D)
				.maxPower(200)
				.idlePower(10)
				.putResources(CPU, mockCPUResource)
				.build();
		var mockServer3 = ImmutableServerArgs.builder()
				.name("test_server3")
				.ownerRegionalManager("test_rma2")
				.jobProcessingLimit(2)
				.price(200D)
				.maxPower(200)
				.idlePower(10)
				.putResources(CPU, mockCPUResource)
				.build();

		var mockRMA1 = ImmutableRegionalManagerArgs.builder()
				.name("test_rma1")
				.build();
		var mockRMA2 = ImmutableRegionalManagerArgs.builder()
				.name("test_rma2")
				.build();

		var mockCMA = ImmutableCentralManagerArgs.builder()
				.name("test_cma")
				.maximumQueueSize(10000)
				.pollingBatchSize(10)
				.build();

		var mockManaging = ImmutableManagingAgentArgs.builder()
				.name("test_managing")
				.systemQualityThreshold(0.8)
				.build();

		scenarioStructureArgs = new ScenarioStructureArgs(
				mockManaging,
				mockCMA,
				List.of(mockRMA1, mockRMA2),
				List.of(mockServer1, mockServer2, mockServer3),
				List.of(mockMonitor1, mockMonitor2, mockMonitor3, mockMonitor4),
				List.of(mockGS1, mockGS2, mockGS3, mockGS4)
		);

	}
}
