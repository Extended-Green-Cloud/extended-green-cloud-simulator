package com.greencloud.application.agents.cloudnetwork.management.logs;

/**
 * Class contains all constants used in logging information in cloud network agent's managers
 */
public class CloudNetworkManagementLog {

	// STATE MANAGEMENT LOG MESSAGES
	public static final String COUNT_JOB_PROCESS_LOG = "Job execution failed. Number of total failed job instances is {}.";
	public static final String COUNT_JOB_ACCEPTED_LOG = "New job was accepted for execution. Number of accepted job instances is {}.";
	public static final String COUNT_JOB_START_LOG = "Started job {}. Number of started jobs is {} out of {} accepted.";
	public static final String COUNT_JOB_FINISH_LOG = "Finished job {}. Number of finished jobs is {} out of {} started.";
	public static final String SAVED_MONITORING_DATA_LOG = "Saved monitoring data for Cloud Network Agent: {}";
}
