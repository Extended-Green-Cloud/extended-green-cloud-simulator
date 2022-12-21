package org.greencloud.managingsystem.service.planner.logs;

/**
 * Class storing log messages used in planner service of managing system
 */
public class ManagingAgentPlannerLog {

	// PLANNER SERVICES LOG MESSAGES
	public static final String NO_ACTIONS_LOG = "There are no actions which can be executed during current system state";
	public static final String SELECTING_BEST_ACTION_LOG = "Selecting best adaptation action...";
	public static final String CONSTRUCTING_PLAN_FOR_ACTION_LOG =
			"Constructing adaptation plan for action {} and passing it to executor...";
	public static final String COULD_NOT_CONSTRUCT_PLAN_LOG =
			"Constructing adaptation plan failed.";

	// PLANS LOGS
	public static final String NO_LOCATION_LOG = "Couldn't find target container for a new server!";
}
