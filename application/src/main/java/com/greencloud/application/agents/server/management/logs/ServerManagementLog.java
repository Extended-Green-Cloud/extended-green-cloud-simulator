package com.greencloud.application.agents.server.management.logs;

/**
 * Class contains all constants used in logging information in server's managers
 */
public class ServerManagementLog {

	// STATE MANAGEMENT LOG MESSAGES
	public static final String COUNT_JOB_PROCESS_LOG = "Processing new job. Number of total processed job instances is {}.";
	public static final String COUNT_JOB_ACCEPTED_LOG = "New job was accepted for execution. Number of accepted job instances is {} out of {} processed";
	public static final String COUNT_JOB_START_LOG = "Started job instance {}. Number of started job instances is {} out of {} accepted";
	public static final String COUNT_JOB_FINISH_LOG = "Finished job instance {}. Number of finished job instances is {} out of {} started and {} processed";
}
