package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;

import org.greencloud.managingsystem.agent.ManagingAgent;

/**
 * Class containing adaptation plan which realizes the action of increasing the job division priority with respect to
 * power
 */
public class IncreaseJobDivisionPowerPriorityPlan extends AbstractPlan {

	public IncreaseJobDivisionPowerPriorityPlan(ManagingAgent managingAgent) {
		super(INCREASE_POWER_PRIORITY, managingAgent);
	}
}
