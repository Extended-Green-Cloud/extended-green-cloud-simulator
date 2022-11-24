package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIO;

import org.greencloud.managingsystem.agent.ManagingAgent;

/**
 * Class containing adaptation plan which realizes the action of increasing the job scheduling priority with respect
 * to deadline
 */
public class IncreaseDeadlinePriorityPlan extends AbstractPlan {

	public IncreaseDeadlinePriorityPlan(ManagingAgent managingAgent) {
		super(INCREASE_DEADLINE_PRIO, managingAgent);
	}
}
