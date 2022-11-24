package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;

import org.greencloud.managingsystem.agent.ManagingAgent;

/**
 * Class containing adaptation plan which realizes the action of connecting new Green Source with given Server
 */
public class AddGreenSourcePlan extends AbstractPlan {

	public AddGreenSourcePlan(ManagingAgent managingAgent) {
		super(ADD_GREEN_SOURCE, managingAgent);
	}
}
