package org.greencloud.managingsystem.service.planner;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIO;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIO;

import java.util.Comparator;
import java.util.Map;
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

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;

/**
 * Service containing methods used in analyzing adaptation options and selecting adaptation plan
 */
public class PlannerService extends AbstractManagingService {

	private final Map<AdaptationActionEnum, AbstractPlan> planForActionMap;

	public PlannerService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.planForActionMap = initializePlansForActions();
	}

	/**
	 * Method is used to trigger the system adaptation planning based on specific adaptation action qualities
	 *
	 * @param adaptationActions set of available adaptation actions with computed qualities
	 */
	public void trigger(final Map<AdaptationAction, Double> adaptationActions) {
		final Map<AbstractPlan, Double> executablePlans =
				adaptationActions.entrySet().stream()
						.filter(entry -> planForActionMap.containsKey(entry.getKey().getAction()))
						.filter(entry -> planForActionMap.get(entry.getKey().getAction()).isPlanExecutable())
						.collect(Collectors.toMap(entry -> getPlanForAdaptationAction(entry.getKey()),
								Map.Entry::getValue));

		if (adaptationActions.isEmpty()) {
			//TODO here print information for user
			return;
		}

		final AbstractPlan constructedPlan = executablePlans.entrySet().stream()
				.max(Comparator.comparingDouble(Map.Entry::getValue))
				.orElseThrow()
				.getKey()
				.constructAdaptationPlan();

		managingAgent.execute().executeAdaptationAction(constructedPlan);
	}

	private AbstractPlan getPlanForAdaptationAction(final AdaptationAction action) {
		return planForActionMap.getOrDefault(action.getAction(), null);
	}

	private Map<AdaptationActionEnum, AbstractPlan> initializePlansForActions() {
		return Map.of(
				ADD_SERVER, new AddServerPlan(managingAgent),
				ADD_GREEN_SOURCE, new AddGreenSourcePlan(managingAgent),
				INCREASE_DEADLINE_PRIO, new IncreaseDeadlinePriorityPlan(managingAgent),
				INCREASE_POWER_PRIO, new IncreaseJobDivisionPowerPriorityPlan(managingAgent),
				INCREASE_GREEN_SOURCE_ERROR, new IncrementGreenSourceErrorPlan(managingAgent),
				INCREASE_GREEN_SOURCE_PERCENTAGE, new IncrementGreenSourcePercentagePlan(managingAgent)
		);
	}
}
