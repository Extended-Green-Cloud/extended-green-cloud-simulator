package org.greencloud.managingsystem.service.planner;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;
import static org.greencloud.managingsystem.service.common.TestAdaptationPlanFactory.getTestAdaptationPlan;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.greencloud.commons.args.adaptation.singleagent.AdjustGreenSourceErrorParameters;
import org.greencloud.commons.args.adaptation.singleagent.ImmutableAdjustGreenSourceErrorParameters;
import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.greencloud.managingsystem.service.planner.plans.ChangeGreenSourceWeightPlan;
import org.greencloud.managingsystem.service.planner.plans.ConnectGreenSourcePlan;
import org.greencloud.managingsystem.service.planner.plans.DecrementGreenSourceErrorPlan;
import org.greencloud.managingsystem.service.planner.plans.IncreaseDeadlinePriorityPlan;
import org.greencloud.managingsystem.service.planner.plans.IncreaseJobDivisionPowerPriorityPlan;
import org.greencloud.managingsystem.service.planner.plans.IncrementGreenSourceErrorPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class PlannerServiceUnitTest {

	@Mock
	private static ManagingAgent managingAgent;
	@Mock
	private static ExecutorService executorService;
	@Mock
	private static MonitoringService monitoringService;
	@Mock
	private static TimescaleDatabase database;
	@Mock
	private static ManagingAgentNode agentNode;

	private PlannerService plannerService;

	private static Stream<Arguments> parametersGetPlanTest() {
		return Stream.of(
				arguments(ADD_SERVER, AddServerPlan.class),
				arguments(CONNECT_GREEN_SOURCE, ConnectGreenSourcePlan.class),
				arguments(INCREASE_DEADLINE_PRIORITY, IncreaseDeadlinePriorityPlan.class),
				arguments(INCREASE_POWER_PRIORITY, IncreaseJobDivisionPowerPriorityPlan.class),
				arguments(INCREASE_GREEN_SOURCE_ERROR, IncrementGreenSourceErrorPlan.class),
				arguments(CHANGE_GREEN_SOURCE_WEIGHT, ChangeGreenSourceWeightPlan.class)
		);
	}

	@BeforeEach
	void init() {
		managingAgent = mock(ManagingAgent.class);
		plannerService = spy(new PlannerService(managingAgent));
		database = mock(TimescaleDatabase.class);
		agentNode = mock(ManagingAgentNode.class);
		monitoringService = spy(new MonitoringService(managingAgent));

		var testStructure = new ScenarioStructureArgs(null, null, emptyList(), emptyList(), emptyList(), emptyList());
		doReturn(testStructure).when(managingAgent).getGreenCloudStructure();
		doReturn(executorService).when(managingAgent).execute();
		doReturn(monitoringService).when(managingAgent).monitor();
		doReturn(database).when(agentNode).getDatabaseClient();
		doReturn(agentNode).when(managingAgent).getAgentNode();
	}

	@Test
	@DisplayName("Test planner trigger for executor not called")
	void testPlannerTriggerForExecutorNotCalled() {
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER).get(0), 30.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY).get(0), 12.0,
				getAdaptationAction(CONNECT_GREEN_SOURCE).get(0), 5.0
		);

		plannerService.trigger(Collections.emptyMap(), MAXIMIZE_JOB_SUCCESS_RATIO);

		verifyNoInteractions(managingAgent.execute());

		plannerService.setPlanForActionMap(Collections.emptyMap());
		plannerService.trigger(testActions, MAXIMIZE_JOB_SUCCESS_RATIO);

		verifyNoInteractions(managingAgent.execute());
	}

	@Test
	@DisplayName("Test planner trigger for executor")
	void testPlannerTriggerForExecutor() {
		final AID mockAgent = mock(AID.class);
		doReturn("test_agent").when(mockAgent).getName();

		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER).get(0), 30.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY).get(0), 12.0,
				getAdaptationAction(CONNECT_GREEN_SOURCE).get(0), 5.0
		);
		plannerService.setPlanForActionMap(Map.of(
				ADD_SERVER, getTestAdaptationPlan(managingAgent, mockAgent,
						ImmutableAdjustGreenSourceErrorParameters.builder().percentageChange(0.07).build())
		));
		doNothing().when(plannerService).initializePlansForActions(MAXIMIZE_JOB_SUCCESS_RATIO);

		plannerService.trigger(testActions, MAXIMIZE_JOB_SUCCESS_RATIO);

		verify(managingAgent).execute();
		verify(executorService).executeAdaptationAction(argThat((plan) ->
				plan.getTargetAgent().equals(mockAgent)
						&& plan.getActionParameters() instanceof AdjustGreenSourceErrorParameters
						&& ((AdjustGreenSourceErrorParameters) plan.getActionParameters()).getPercentageChange()
						== 0.07));
	}

	@Test
	@DisplayName("Test planner trigger for selection of green source increment error plan")
	void testPlannerTriggerForIncrementingGreenSourceErrorPlan() {
		plannerService.setPlanForActionMap(Map.of(
				INCREASE_GREEN_SOURCE_ERROR,
				new IncrementGreenSourceErrorPlan(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO)
		));
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(INCREASE_GREEN_SOURCE_ERROR).get(0), 20.0
		);
		mockHealthCheckData();
		doReturn(prepareGSData()).when(database).readLastMonitoringDataForDataTypes(List.of(GREEN_SOURCE_MONITORING));
		doReturn(preparePowerShortageData()).when(database)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), eq(List.of("test_gs1", "test_gs2")),
						anyDouble());
		plannerService.trigger(testActions, MAXIMIZE_JOB_SUCCESS_RATIO);

		verify(managingAgent).execute();
		verify(executorService).executeAdaptationAction(argThat((val) ->
				val.getTargetAgent().getName().equals("test_gs2") &&
						val.getActionParameters() instanceof AdjustGreenSourceErrorParameters));
	}

	@Test
	@DisplayName("Test planner trigger for selection of green source decrement error plan")
	void testPlannerTriggerForDecrementingGreenSourceErrorPlan() {
		plannerService.setPlanForActionMap(Map.of(
				DECREASE_GREEN_SOURCE_ERROR,
				new DecrementGreenSourceErrorPlan(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO)
		));
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(DECREASE_GREEN_SOURCE_ERROR).get(0), 20.0
		);
		mockHealthCheckData();
		prepareGreenSourceStructure();

		var testAdaptationGoal = new AdaptationGoal(2, "MINIMIZE_USED_BACKUP_POWER", 0.2, true, 0.7);

		doReturn(testAdaptationGoal).when(monitoringService)
				.getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER);
		doReturn(prepareServerData()).when(database)
				.readMonitoringDataForDataTypeAndAID(eq(SERVER_MONITORING), eq(List.of("test_server1")),
						anyDouble());
		doReturn(prepareGSData()).when(database).readLastMonitoringDataForDataTypes(List.of(GREEN_SOURCE_MONITORING));
		doReturn(preparePowerShortageData()).when(database)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), eq(List.of("test_gs3")),
						anyDouble());
		plannerService.trigger(testActions, MAXIMIZE_JOB_SUCCESS_RATIO);

		verify(managingAgent).execute();
		verify(executorService).executeAdaptationAction(argThat((val) ->
				val.getTargetAgent().getName().equals("test_gs3") &&
						val.getActionParameters() instanceof AdjustGreenSourceErrorParameters));
	}

	private void prepareGreenSourceStructure() {
		var structure = mock(ScenarioStructureArgs.class);
		doReturn(List.of("test_gs3")).when(structure).getGreenSourcesForServerAgent("test_server1");
		doReturn(structure).when(managingAgent).getGreenCloudStructure();
	}

	private List<AgentData> prepareServerData() {
		var data1 = ImmutableServerMonitoringData.builder()
				.successRatio(0.8)
				.currentTraffic(0.4)
				.isDisabled(false)
				.serverJobs(10)
				.idlePowerConsumption(10)
				.currentBackUpPowerTraffic(0.6)
				.currentPowerConsumption(0.7)
				.build();

		return List.of(
				new AgentData(now(), "test_server1", SERVER_MONITORING, data1)
		);
	}

	private List<AgentData> prepareGSData() {
		var data1 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.8)
				.successRatio(0.7)
				.weatherPredictionError(0.02)
				.isBeingDisconnected(false)
				.build();
		var data2 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.8)
				.successRatio(0.7)
				.weatherPredictionError(0.05)
				.isBeingDisconnected(false)
				.build();
		var data3 = ImmutableGreenSourceMonitoringData.builder()
				.currentTraffic(0.8)
				.successRatio(0.7)
				.weatherPredictionError(1.0)
				.isBeingDisconnected(false)
				.build();

		return List.of(
				new AgentData(now(), "test_gs1", GREEN_SOURCE_MONITORING, data1),
				new AgentData(now(), "test_gs2", GREEN_SOURCE_MONITORING, data2),
				new AgentData(now(), "test_gs3", GREEN_SOURCE_MONITORING, data3)
		);
	}

	private List<AgentData> preparePowerShortageData() {
		return List.of(
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, new WeatherShortages(1, 1000)),
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, new WeatherShortages(2, 1000)),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, new WeatherShortages(3, 1000)),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, new WeatherShortages(1, 1000)),
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, new WeatherShortages(1, 1000))
		);
	}

	private void mockHealthCheckData() {
		var healthCheck1 = new HealthCheck(true, EGCSAgentType.GREEN_ENERGY);
		var healthCheck2 = new HealthCheck(true, EGCSAgentType.GREEN_ENERGY);
		var healthCheck3 = new HealthCheck(true, EGCSAgentType.GREEN_ENERGY);
		var healthCheck4 = new HealthCheck(true, EGCSAgentType.SERVER);

		var mockData = List.of(
				new AgentData(now(), "test_gs1", HEALTH_CHECK, healthCheck1),
				new AgentData(now(), "test_gs2", HEALTH_CHECK, healthCheck2),
				new AgentData(now(), "test_gs3", HEALTH_CHECK, healthCheck3),
				new AgentData(now(), "test_server1", HEALTH_CHECK, healthCheck4)
		);

		doReturn(mockData).when(database).readLastMonitoringDataForDataTypes(Collections.singletonList(HEALTH_CHECK),
				MONITOR_SYSTEM_DATA_HEALTH_PERIOD);
	}

	@ParameterizedTest
	@MethodSource("parametersGetPlanTest")
	@DisplayName("Test getting plan for adaptation action")
	void testGetPlanForAdaptationAction(final AdaptationActionEnum adaptation, final Class<?> expectedPlan) {
		plannerService.initializePlansForActions(MAXIMIZE_JOB_SUCCESS_RATIO);
		assertThat(plannerService.getPlanForAdaptationAction(getAdaptationAction(adaptation).get(0)))
				.isInstanceOf(expectedPlan);
	}

	@Test
	@DisplayName("Test getting plans which can be executed")
	void testGetPlansWhichCanBeExecuted() {
		final AbstractPlan plan1 = new AbstractPlan(ADD_SERVER, managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO) {
			@Override
			public boolean isPlanExecutable() {
				return false;
			}

			@Override
			public AbstractPlan constructAdaptationPlan() {
				return this;
			}
		};
		final AbstractPlan plan2 = new AbstractPlan(INCREASE_DEADLINE_PRIORITY, managingAgent,
				MAXIMIZE_JOB_SUCCESS_RATIO) {
			@Override
			public boolean isPlanExecutable() {
				return true;
			}

			@Override
			public AbstractPlan constructAdaptationPlan() {
				return this;
			}
		};
		plannerService.setPlanForActionMap(Map.of(ADD_SERVER, plan1, INCREASE_DEADLINE_PRIORITY, plan2));

		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER).get(0), 10.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY).get(0), 12.0,
				getAdaptationAction(CONNECT_GREEN_SOURCE).get(0), 5.0
		);

		var result = plannerService.getPlansWhichCanBeExecuted(testActions);

		assertThat(result.entrySet())
				.as("Result should have size 1")
				.hasSize(1)
				.as("Result should contain correct field")
				.allSatisfy((entry) -> {
					assertThat(entry.getKey()).isEqualTo(getAdaptationAction(INCREASE_DEADLINE_PRIORITY).get(0));
					assertThat(entry.getValue()).isEqualTo(12.0);
				});
	}

	@Test
	@DisplayName("Test selection of the best action")
	void testSelectBestAction() {
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER).get(0), 30.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY).get(0), 12.0,
				getAdaptationAction(CONNECT_GREEN_SOURCE).get(0), 5.0
		);

		var result = plannerService.selectBestAction(testActions);

		assertThat(result).isEqualTo(getAdaptationAction(ADD_SERVER).get(0));
	}

	@Test
	@DisplayName("Test selection of the best action for map size 1")
	void testSelectBestActionMapSize1() {
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER).get(0), 30.0
		);

		var result = plannerService.selectBestAction(testActions);

		assertThat(result).isEqualTo(getAdaptationAction(ADD_SERVER).get(0));
	}
}
