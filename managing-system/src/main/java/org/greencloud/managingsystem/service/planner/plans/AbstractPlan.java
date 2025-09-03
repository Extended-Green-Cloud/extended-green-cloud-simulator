package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.types.GoalType;

import jade.core.AID;
import lombok.Getter;

/**
 * Abstract class which should be extended by each adaptation plan class
 */
@Getter
public abstract class AbstractPlan {

	protected final ManagingAgent managingAgent;
	protected final AdaptationActionTypeEnum adaptationActionEnum;
	protected AdaptationActionParameters actionParameters;
	protected AID targetAgent;
	protected GoalType violatedGoal;

	protected Runnable postActionHandler;

	/**
	 * Default abstract constructor
	 *
	 * @param actionEnum    type of adaptation action
	 * @param managingAgent managing agent executing the action
	 * @param violatedGoal  goal that has been violated and requires adaptation
	 */
	protected AbstractPlan(final AdaptationActionTypeEnum actionEnum,
			final ManagingAgent managingAgent,
			final GoalType violatedGoal) {
		this.adaptationActionEnum = actionEnum;
		this.managingAgent = managingAgent;
		this.violatedGoal = violatedGoal;
	}

	/**
	 * Abstract method verifies if the plan can be executed taking into account specific
	 * constraints and the current state of the system
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	public abstract boolean isPlanExecutable();

	/**
	 * Abstract method used for creation of the adaptation plan
	 *
	 * @return prepared adaptation plan
	 */
	public abstract AbstractPlan constructAdaptationPlan();

	/**
	 * Method returns the function that disables the execution of adaptation action that corresponds to the given plan.
	 * It is used particularly in the ExecutorService after a given plan is enacted on the system in order to enable
	 * gathering the results of a selected adaptation.
	 */
	public Runnable disablePlanAction() {
		return () -> {
			final AdaptationAction action = getAdaptationAction(adaptationActionEnum, violatedGoal);

			managingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(action.getActionId(), false);
		};
	}

	/**
	 * Method enables the execution of adaptation action that corresponds to the given plan.
	 */
	public Runnable enablePlanAction() {
		return () -> {
			final AdaptationAction action = getAdaptationAction(adaptationActionEnum, violatedGoal);

			managingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(action.getActionId(), true);
		};
	}
}
