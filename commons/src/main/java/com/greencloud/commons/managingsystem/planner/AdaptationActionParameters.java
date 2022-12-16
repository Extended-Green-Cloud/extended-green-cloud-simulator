package com.greencloud.commons.managingsystem.planner;

/**
 * Abstract interface prepared for the content of adaptation plans.
 * It has to be inherited by all other structures sent as content of the executor messages
 */
public interface AdaptationActionParameters {

	/**
	 * @return flag indicating if the agent's adaptation listener should respond with adaptation result after performing
	 * adaptation plan locally, or should it wait for additional responses of other agents
	 */
	default boolean dependsOnOtherAgents() {
		return false;
	}
}
