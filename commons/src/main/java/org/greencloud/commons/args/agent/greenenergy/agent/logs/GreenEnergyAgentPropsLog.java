package org.greencloud.commons.args.agent.greenenergy.agent.logs;

/**
 * Class contains all constants used in logging information in green energy agent's managers
 */
public class GreenEnergyAgentPropsLog {

	// STATE MANAGEMENT LOG MESSAGES
	public static final String POWER_JOB_START_LOG =
			"Started job instance {}. Number of started job instances is {} out of {} accepted";
	public static final String POWER_JOB_FINISH_LOG =
			"Finished job instance {}. Number of finished job instances is {} out of {} started";
	public static final String POWER_JOB_ACCEPTED_LOG =
			"New server job was accepted for execution. Number of accepted job instances is {}.";
	public static final String POWER_JOB_FAILED_LOG =
			"Server job execution failed. Number of total failed job instances is {}.";
	public static final String CURRENT_AVAILABLE_POWER_LOG = "Calculated available {} power {} at {}";

}
