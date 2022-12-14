package org.greencloud.managingsystem.service.planner.plans;

import com.greencloud.commons.managingsystem.planner.SystemAdaptationActionParameters;

/**
 * Interface used to differentiate plans that have to be executed over Jade Framework in which Green Cloud network
 * resides as opposed to plans executed on particular agents.
 */
public interface SystemPlan {

	SystemAdaptationActionParameters getSystemAdaptationActionParameters();
}
