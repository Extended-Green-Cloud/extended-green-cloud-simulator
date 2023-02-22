package org.greencloud.managingsystem.service.planner.plans.domain;

/**
 * Class stores all variables defined at the system start which are used in adaptation plans of managing agent
 * <p> POWER_SHORTAGE_THRESHOLD 			 - minimum number of power drops necessary for the GS to be considered for the
 * 								  			   increment prediction error adaptation plan </p>
 */
public class AdaptationPlanVariables {

	// DECREMENT/INCREMENT ERROR PLAN
	public static int POWER_SHORTAGE_THRESHOLD = 2;
}
