package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;

import org.greencloud.managingsystem.agent.ManagingAgent;

/**
 * Class containing adaptation plan which realizes the action of adding new server to the system
 */
public class AddServerPlan extends AbstractPlan {

	public AddServerPlan(ManagingAgent managingAgent) {
		super(ADD_SERVER, managingAgent);
	}

	@Override
	public boolean isPlanExecutable() {
		return false;
	}

	@Override
	public AbstractPlan constructAdaptationPlan() {
		return this;
	}
}
