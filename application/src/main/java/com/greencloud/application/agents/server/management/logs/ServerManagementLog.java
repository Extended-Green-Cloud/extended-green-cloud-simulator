package com.greencloud.application.agents.server.management.logs;

/**
 * Class contains all constants used in logging information in server's managers
 */
public class ServerManagementLog {

	// STATE MANAGEMENT LOG MESSAGES
	public static final String COUNT_JOB_PROCESS_LOG =
			"Job execution failed. Number of total failed job instances is {}.";
	public static final String COUNT_JOB_ACCEPTED_LOG =
			"New job instance was accepted for execution. Number of accepted job instances is {}.";
	public static final String COUNT_JOB_START_LOG =
			"Started job instance {}. Number of started job instances is {} out of {} accepted.";
	public static final String COUNT_JOB_FINISH_LOG =
			"Finished job instance {}. Number of finished job instances is {} out of {} started.";

	// ADAPTATION MANAGEMENT LOG MESSAGES
	public static final String DISABLE_SERVER_LOG = "Disabling Server and informing CNA {}.";
}
