package org.greencloud.managingsystem.service.planner;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static org.greencloud.managingsystem.service.planner.logs.ManagingAgentPlannerLog.CONSTRUCTING_PLAN_FOR_ACTION_LOG;
import static org.greencloud.managingsystem.service.planner.logs.ManagingAgentPlannerLog.COULD_NOT_CONSTRUCT_PLAN_LOG;
import static org.greencloud.managingsystem.service.planner.logs.ManagingAgentPlannerLog.NO_ACTIONS_LOG;
import static org.greencloud.managingsystem.service.planner.logs.ManagingAgentPlannerLog.SELECTING_BEST_ACTION_LOG;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddGreenSourcePlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.greencloud.managingsystem.service.planner.plans.IncreaseDeadlinePriorityPlan;
import org.greencloud.managingsystem.service.planner.plans.IncreaseJobDivisionPowerPriorityPlan;
import org.greencloud.managingsystem.service.planner.plans.IncrementGreenSourceErrorPlan;
import org.greencloud.managingsystem.service.planner.plans.IncrementGreenSourcePercentagePlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.google.common.annotations.VisibleForTesting;

/**
 * Service containing methods used in analyzing adaptation options and selecting adaptation plan
 */
public class PlannerService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(PlannerService.class);

	private Map<AdaptationActionEnum, AbstractPlan> planForActionMap;

	public PlannerService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method is used to trigger the system adaptation planning based on specific adaptation action qualities
	 *
	 * @param adaptationActions set of available adaptation actions with computed qualities
	 */
	public void trigger(final Map<AdaptationAction, Double> adaptationActions) {
		initializePlansForActions();
		final Map<AdaptationAction, Double> executableActions = getPlansWhichCanBeExecuted(adaptationActions);

		if (executableActions.isEmpty()) {
			logger.info(NO_ACTIONS_LOG);
			return;
		}

		logger.info(SELECTING_BEST_ACTION_LOG);
		final AdaptationAction bestAction = selectBestAction(executableActions);
		final AbstractPlan constructedPlan = getPlanForAdaptationAction(bestAction).constructAdaptationPlan();
		final boolean isPlanConstructed = Objects.nonNull(constructedPlan.constructAdaptationPlan()) && Objects.nonNull(
				constructedPlan.getTargetAgent());

		if (!isPlanConstructed) {
			logger.info(COULD_NOT_CONSTRUCT_PLAN_LOG);
			return;
		}

		logger.info(CONSTRUCTING_PLAN_FOR_ACTION_LOG, constructedPlan.getAdaptationActionEnum().getName());
		managingAgent.execute().executeAdaptationAction(constructedPlan);
	}

	protected void setPlanForActionMap(Map<AdaptationActionEnum, AbstractPlan> planForActionMap) {
		this.planForActionMap = planForActionMap;
	}

	@VisibleForTesting
	protected AdaptationAction selectBestAction(final Map<AdaptationAction, Double> adaptationActions) {
		return adaptationActions.entrySet().stream()
				.filter(action -> action.getKey().getAvailable())
				.max(Comparator.comparingDouble(Map.Entry::getValue))
				.orElseThrow()
				.getKey();
	}

	@VisibleForTesting
	protected Map<AdaptationAction, Double> getPlansWhichCanBeExecuted(
			final Map<AdaptationAction, Double> adaptationActions) {
		return adaptationActions.entrySet().stream()
				.filter(entry -> planForActionMap.containsKey(entry.getKey().getAction()))
				.filter(entry -> planForActionMap.get(entry.getKey().getAction()).isPlanExecutable())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected AbstractPlan getPlanForAdaptationAction(final AdaptationAction action) {
		return planForActionMap.getOrDefault(action.getAction(), null);
	}

	protected void initializePlansForActions() {
		planForActionMap = Map.of(
				ADD_SERVER, new AddServerPlan(managingAgent),
				ADD_GREEN_SOURCE, new AddGreenSourcePlan(managingAgent),
				INCREASE_DEADLINE_PRIORITY, new IncreaseDeadlinePriorityPlan(managingAgent),
				INCREASE_POWER_PRIORITY, new IncreaseJobDivisionPowerPriorityPlan(managingAgent),
				INCREASE_GREEN_SOURCE_ERROR, new IncrementGreenSourceErrorPlan(managingAgent),
				INCREASE_GREEN_SOURCE_PERCENTAGE, new IncrementGreenSourcePercentagePlan(managingAgent)
		);
	}
}
