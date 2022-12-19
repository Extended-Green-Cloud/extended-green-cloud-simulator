package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.agent.DataType.SHORTAGES;
import static com.google.common.collect.ImmutableList.of;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.application.yellowpages.YellowPagesService;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceWeights;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
class ChangeGreenSourceWeightPlanUnitTest {

	@Mock
	private ManagingAgent managingAgent;
	@Mock
	private ManagingAgentNode managingAgentNode;
	@Mock
	private TimescaleDatabase timescaleDatabase;
	private MockedStatic<YellowPagesService> yellowPagesService;

	private ChangeGreenSourceWeightPlan changeGreenSourceWeightPlan;
	private ScenarioStructureArgs greenCloudStructure;
	private String serverName;

	@BeforeEach
	void init() {
		yellowPagesService = mockStatic(YellowPagesService.class);
		yellowPagesService.when(() -> YellowPagesService.search(managingAgent, SA_SERVICE_TYPE))
				.thenReturn(Set.of(new AID("Server1", AID.ISGUID)));
		changeGreenSourceWeightPlan = new ChangeGreenSourceWeightPlan(managingAgent);
		GreenEnergyAgentArgs greenEnergyAgentArgs1 = ImmutableGreenEnergyAgentArgs.builder()
				.weatherPredictionError("0.2")
				.energyType("WIND")
				.latitude("50")
				.longitude("50")
				.maximumCapacity("100")
				.name("Wind1")
				.pricePerPowerUnit("5")
				.ownerSever("Server1")
				.monitoringAgent("MonitoringAgent1")
				.build();
		GreenEnergyAgentArgs greenEnergyAgentArgs2 = ImmutableGreenEnergyAgentArgs.builder()
				.from(greenEnergyAgentArgs1)
				.name("Wind2")
				.build();

		greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(), emptyList(), emptyList(),
				List.of(greenEnergyAgentArgs1, greenEnergyAgentArgs2));

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(timescaleDatabase);
	}

	@AfterEach
	void cleanUp() {
		yellowPagesService.close();
	}

	@ParameterizedTest
	@MethodSource("shortagesProvider")
	void shouldCorrectlyTestIfPlanIsExecutable(List<AgentData> shortages, boolean expectedResult) {
		// given
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES))).thenReturn(shortages);

		// when
		var result = changeGreenSourceWeightPlan.isPlanExecutable();

		// then
		assertThat(result).isEqualTo(expectedResult);
	}

	private static Stream<Arguments> shortagesProvider() {
		return Stream.of(
				arguments(of(generateTestData("Wind1")), true),
				arguments(of(generateTestData("Wind1"), generateTestData("Wind2")), true),
				arguments(emptyList(), false)
		);
	}

	@Test
	void shouldReturnFalseIfNoNewShortagesHappened() {
		// given
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES)))
				.thenReturn(of(generateTestData("Wind1")));
		var firstResult = changeGreenSourceWeightPlan.isPlanExecutable();

		// when
		var secondResult = changeGreenSourceWeightPlan.isPlanExecutable();

		// then
		assertThat(firstResult).isTrue();
		assertThat(secondResult).isFalse();
	}

	@Test
	void shouldReturnTrueIfNewShortagesHappened() {
		// given
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES)))
				.thenReturn(of(generateTestData("Wind1")));
		var firstResult = changeGreenSourceWeightPlan.isPlanExecutable();
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES)))
				.thenReturn(of(generateTestData("Wind1", 15)));

		// when
		var secondResult = changeGreenSourceWeightPlan.isPlanExecutable();

		// then
		assertThat(firstResult).isTrue();
		assertThat(secondResult).isTrue();
	}

	@Test
	void shouldCorrectlyBuildAdaptationPlan() {
		// given
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES)))
				.thenReturn(of(generateTestData("Wind1")));
		var isPlanExecutable = changeGreenSourceWeightPlan.isPlanExecutable();

		// when
		var result = changeGreenSourceWeightPlan.constructAdaptationPlan();

		// then
		assertThat(isPlanExecutable).isTrue();
		assertThat(result.getTargetAgent()).isEqualTo("Server1");
		assertThat(result.adaptationActionEnum).isEqualTo(CHANGE_GREEN_SOURCE_WEIGHT);
		assertThat(result.getActionParameters()).isInstanceOf(ChangeGreenSourceWeights.class);
		var params = (ChangeGreenSourceWeights) result.getActionParameters();
		assertThat(params.greenSourceName()).isEqualTo("Wind1");
	}

	private static AgentData generateTestData(String agentName) {
		return generateTestData(agentName, 10);
	}

	private static AgentData generateTestData(String agentName, int numberOfShortages) {
		return new AgentData(now(), agentName, SHORTAGES, new Shortages(numberOfShortages));
	}
}
