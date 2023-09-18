package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.agent.DataType.SHORTAGES;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.google.common.collect.ImmutableList.of;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.commons.constants.DFServiceConstants.SA_SERVICE_TYPE;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.search;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.greencloud.commons.args.agent.greenenergy.factory.ImmutableGreenEnergyArgs;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.commons.utils.yellowpages.YellowPagesRegister;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.timescale.TimescaleDatabase;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceWeights;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import com.gui.agents.managing.ManagingAgentNode;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangeGreenSourceWeightPlanUnitTest {

	@Mock
	private ManagingAgent managingAgent;
	@Mock
	private ManagingAgentNode managingAgentNode;
	@Mock
	private TimescaleDatabase timescaleDatabase;
	@Mock
	private MonitoringService mockMonitoring;
	private MockedStatic<YellowPagesRegister> yellowPagesService;

	private ChangeGreenSourceWeightPlan changeGreenSourceWeightPlan;
	private ScenarioStructureArgs greenCloudStructure;

	@BeforeEach
	void init() {
		yellowPagesService = mockStatic(YellowPagesRegister.class);
		mockMonitoring = spy(new MonitoringService(managingAgent));
		yellowPagesService.when(() -> search(eq(managingAgent), any(), eq(SA_SERVICE_TYPE)))
				.thenReturn(Set.of(new AID("Server1", AID.ISGUID)));
		changeGreenSourceWeightPlan = new ChangeGreenSourceWeightPlan(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO);
		GreenEnergyArgs greenEnergyAgentArgs1 = ImmutableGreenEnergyArgs.builder()
				.weatherPredictionError(0.2)
				.energyType(WIND)
				.latitude("50")
				.longitude("50")
				.maximumCapacity(100L)
				.name("Wind1")
				.pricePerPowerUnit(5L)
				.ownerSever("Server1")
				.monitoringAgent("MonitoringAgent1")
				.build();
		GreenEnergyArgs greenEnergyAgentArgs2 = ImmutableGreenEnergyArgs.builder()
				.from(greenEnergyAgentArgs1)
				.name("Wind2")
				.build();

		greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(), emptyList(), emptyList(),
				List.of(greenEnergyAgentArgs1, greenEnergyAgentArgs2));

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(timescaleDatabase);
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(managingAgent.monitor()).thenReturn(mockMonitoring);

		when(mockMonitoring.getActiveServers()).thenReturn(List.of("Server1"));
	}

	@AfterEach
	void cleanUp() {
		yellowPagesService.close();
		ChangeGreenSourceWeightPlan.greenSourceExecutedActions.clear();
		ChangeGreenSourceWeightPlan.greenSourceAccumulatedShortages.clear();
	}

	@ParameterizedTest
	@MethodSource("shortagesProvider")
	void shouldCorrectlyTestIfPlanIsExecutable(List<AgentData> shortages, boolean expectedResult) {
		// given
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES))).thenReturn(shortages);
		when(mockMonitoring.getAliveAgents(GREEN_ENERGY)).thenReturn(List.of("Wind1", "Wind2"));
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
		when(mockMonitoring.getAliveAgents(GREEN_ENERGY)).thenReturn(List.of("Wind1", "Wind2"));
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
		when(mockMonitoring.getAliveAgents(GREEN_ENERGY)).thenReturn(List.of("Wind1", "Wind2"));
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
		when(mockMonitoring.getAliveAgents(GREEN_ENERGY)).thenReturn(List.of("Wind1"));
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SHORTAGES)))
				.thenReturn(of(generateTestData("Wind1")));
		var isPlanExecutable = changeGreenSourceWeightPlan.isPlanExecutable();

		// when
		var result = changeGreenSourceWeightPlan.constructAdaptationPlan();

		// then
		assertThat(isPlanExecutable).isTrue();
		assertThat(result.getTargetAgent().getName()).isEqualTo("Server1");
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
