package org.greencloud.managingsystem.service.planner;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddGreenSourcePlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.greencloud.managingsystem.service.planner.plans.IncreaseDeadlinePriorityPlan;
import org.greencloud.managingsystem.service.planner.plans.IncreaseJobDivisionPowerPriorityPlan;
import org.greencloud.managingsystem.service.planner.plans.IncrementGreenSourceErrorPlan;
import org.greencloud.managingsystem.service.planner.plans.IncrementGreenSourcePercentagePlan;
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
import com.greencloud.commons.managingsystem.planner.ImmutableIncrementGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class PlannerServiceUnitTest {

	@Mock
	private static ManagingAgent managingAgent;
	@Mock
	private static ExecutorService executorService;

	private PlannerService plannerService;

	private static Stream<Arguments> parametersGetPlanTest() {
		return Stream.of(
				arguments(ADD_SERVER, AddServerPlan.class),
				arguments(ADD_GREEN_SOURCE, AddGreenSourcePlan.class),
				arguments(INCREASE_DEADLINE_PRIORITY, IncreaseDeadlinePriorityPlan.class),
				arguments(INCREASE_POWER_PRIORITY, IncreaseJobDivisionPowerPriorityPlan.class),
				arguments(INCREASE_GREEN_SOURCE_ERROR, IncrementGreenSourceErrorPlan.class),
				arguments(INCREASE_GREEN_SOURCE_PERCENTAGE, IncrementGreenSourcePercentagePlan.class)
		);
	}

	@BeforeEach
	void init() {
		plannerService = new PlannerService(managingAgent);
		doReturn(executorService).when(managingAgent).execute();
	}

	@Test
	@DisplayName("Test planner trigger for executor not called")
	void testPlannerTriggerForExecutorNotCalled() {
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER), 30.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY), 12.0,
				getAdaptationAction(ADD_GREEN_SOURCE), 5.0
		);

		plannerService.trigger(Collections.emptyMap());

		verifyNoInteractions(managingAgent.execute());

		plannerService.setPlanForActionMap(Collections.emptyMap());
		plannerService.trigger(testActions);

		verifyNoInteractions(managingAgent.execute());
	}

	@Test
	@DisplayName("Test planner trigger for executor")
	void testPlannerTriggerForExecutor() {
		final AID mockAgent = mock(AID.class);
		doReturn("test_agent").when(mockAgent).getName();

		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER), 30.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY), 12.0,
				getAdaptationAction(ADD_GREEN_SOURCE), 5.0
		);
		plannerService.setPlanForActionMap(Map.of(
				ADD_SERVER, new AbstractPlan(ADD_SERVER, managingAgent) {
					@Override
					public boolean isPlanExecutable() {
						return true;
					}

					@Override
					public AbstractPlan constructAdaptationPlan() {
						actionParameters = ImmutableIncrementGreenSourceErrorParameters.builder()
								.percentageChange(10.0)
								.build();
						targetAgent = mockAgent;
						return this;
					}
				}
		));

		plannerService.trigger(testActions);

		verify(managingAgent).execute();
		verify(executorService).executeAdaptationAction(argThat((plan) -> plan.getTargetAgent().equals(mockAgent)
				&& plan.getActionParameters() instanceof IncrementGreenSourceErrorParameters
				&& ((IncrementGreenSourceErrorParameters) plan.getActionParameters()).getPercentageChange() == 10.0));

	}

	@ParameterizedTest
	@MethodSource("parametersGetPlanTest")
	@DisplayName("Test getting plan for adaptation action")
	void testGetPlanForAdaptationAction(final AdaptationActionEnum adaptation, final Class<?> expectedPlan) {
		assertThat(plannerService.getPlanForAdaptationAction(getAdaptationAction(adaptation))).isInstanceOf(
				expectedPlan);
	}

	@Test
	@DisplayName("Test getting plans which can be executed")
	void testGetPlansWhichCanBeExecuted() {
		final AbstractPlan plan1 = new AbstractPlan(ADD_SERVER, managingAgent) {
			@Override
			public boolean isPlanExecutable() {
				return false;
			}
		};
		final AbstractPlan plan2 = new AbstractPlan(INCREASE_DEADLINE_PRIORITY, managingAgent) {
			@Override
			public boolean isPlanExecutable() {
				return true;
			}
		};
		plannerService.setPlanForActionMap(Map.of(ADD_SERVER, plan1, INCREASE_DEADLINE_PRIORITY, plan2));

		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER), 10.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY), 12.0,
				getAdaptationAction(ADD_GREEN_SOURCE), 5.0
		);

		var result = plannerService.getPlansWhichCanBeExecuted(testActions);

		assertThat(result.entrySet())
				.as("Result should have size 1")
				.hasSize(1)
				.as("Result should contain correct field")
				.allSatisfy((entry) -> {
					assertThat(entry.getKey()).isEqualTo(getAdaptationAction(INCREASE_DEADLINE_PRIORITY));
					assertThat(entry.getValue()).isEqualTo(12.0);
				});
	}

	@Test
	@DisplayName("Test selection of the best action")
	void testSelectBestAction() {
		final Map<AdaptationAction, Double> testActions = Map.of(
				getAdaptationAction(ADD_SERVER), 30.0,
				getAdaptationAction(INCREASE_DEADLINE_PRIORITY), 12.0,
				getAdaptationAction(ADD_GREEN_SOURCE), 5.0
		);

		var result = plannerService.selectBestAction(testActions);

		assertThat(result).isEqualTo(getAdaptationAction(ADD_SERVER));
	}
}
