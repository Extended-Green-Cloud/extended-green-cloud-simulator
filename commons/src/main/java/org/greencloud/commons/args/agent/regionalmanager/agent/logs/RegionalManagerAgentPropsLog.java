package org.greencloud.commons.args.agent.regionalmanager.agent.logs;

/**
 * Class contains all constants used in logging information in regional manager agent's props
 */
public class RegionalManagerAgentPropsLog {

	// STATE MANAGEMENT LOG MESSAGES
	public static final String COUNT_JOB_PROCESS_LOG = "Job execution failed. Number of total failed job instances is {}.";
	public static final String COUNT_JOB_ACCEPTED_LOG = "New job was accepted for execution. Number of accepted job instances is {}.";
	public static final String COUNT_JOB_START_LOG = "Started job {}. Number of started jobs is {} out of {} accepted.";
	public static final String COUNT_JOB_FINISH_LOG = "Finished job {}. Number of finished jobs is {} out of {} started.";
}
