package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static java.util.Objects.nonNull;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.GoalEnum;
import com.greencloud.commons.managingsystem.planner.ImmutableIncreaseJobDivisionPriorityParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of increasing the job division priority with respect to
 * the job's power
 */
public class IncreaseJobDivisionPowerPriorityPlan extends AbstractPlan {

	public IncreaseJobDivisionPowerPriorityPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(INCREASE_POWER_PRIORITY, managingAgent, violatedGoal);
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. the Scheduler Agent is alive
	 * 2. power priority percentage is less than 100
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final String aliveScheduler = managingAgent.monitor().getAliveScheduler();

		if (nonNull(aliveScheduler)) {
			targetAgent = new AID(aliveScheduler, AID.ISGUID);
		}
		return nonNull(aliveScheduler);
	}

	/**
	 * Method creates adaptation plan which increases (to next Fibonacci number)
	 * the job scheduling priority with respect to job's power
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		actionParameters = ImmutableIncreaseJobDivisionPriorityParameters.builder().build();
		return this;
	}

	/**
	 * Method disables the INCREASE_POWER_PRIORITY along with its corresponding INCREASE_DEADLINE_PRIORITY action
	 */
	@Override
	public Runnable disablePlanAction() {
		return () -> {
			super.disablePlanAction();
			changeDeadlinePriorityActionAvailability(false);
		};
	}

	/**
	 * Method enables the INCREASE_POWER_PRIORITY along with its corresponding INCREASE_DEADLINE_PRIORITY action
	 */
	@Override
	public Runnable enablePlanAction() {
		return () -> {
			super.disablePlanAction();
			changeDeadlinePriorityActionAvailability(true);
		};
	}

	private void changeDeadlinePriorityActionAvailability(boolean availability) {
		final AdaptationAction increaseDeadlineAction = getAdaptationAction(INCREASE_DEADLINE_PRIORITY, violatedGoal);

		managingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(increaseDeadlineAction.getActionId(), availability);
	}
}
