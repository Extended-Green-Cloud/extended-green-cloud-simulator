package org.greencloud.managingsystem.service.planner;

import java.util.Map;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.database.knowledge.domain.action.AdaptationAction;

/**
 * Service containing methods used in analyzing adaptation options and selecting adaptation plan
 */
public class PlannerService extends AbstractManagingService {

	public PlannerService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method is used to trigger the system adaptation planning based on specific adaptation action qualities
	 *
	 * @param adaptationActions set of available adaptation actions with computed qualities
	 */
	public void trigger(final Map<AdaptationAction, Double> adaptationActions) {
		//TODO implementation of planner in next PR
	}
}
