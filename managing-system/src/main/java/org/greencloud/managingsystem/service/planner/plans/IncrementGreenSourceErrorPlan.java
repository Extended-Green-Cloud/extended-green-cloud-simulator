package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;

import org.greencloud.managingsystem.agent.ManagingAgent;

/**
 * Class containing adaptation plan which realizes the action of incrementing
 * the weather prediction error for given Green Source
 */
public class IncrementGreenSourceErrorPlan extends AbstractPlan {

	public IncrementGreenSourceErrorPlan(ManagingAgent managingAgent) {
		super(INCREASE_GREEN_SOURCE_ERROR, managingAgent);
	}
}
