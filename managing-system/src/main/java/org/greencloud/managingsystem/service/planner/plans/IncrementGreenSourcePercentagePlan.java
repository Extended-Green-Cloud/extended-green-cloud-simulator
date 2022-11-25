package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;

import org.greencloud.managingsystem.agent.ManagingAgent;

/**
 * Class containing adaptation plan which realizes the action of incrementing the chance of selection of given Green
 * Source for specified Server
 */
public class IncrementGreenSourcePercentagePlan extends AbstractPlan {

	public IncrementGreenSourcePercentagePlan(ManagingAgent managingAgent) {
		super(INCREASE_GREEN_SOURCE_PERCENTAGE, managingAgent);
	}
}
