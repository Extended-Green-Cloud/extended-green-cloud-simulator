package com.greencloud.application.agents.scheduler.managment.logs;

/**
 * Class contains all constants used in logging information in scheduler's managers
 */
public class SchedulerManagementLog {

	// STATE MANAGER
	public static final String FULL_JOBS_QUEUE_LOG = "Postponed job {} was successfully added but the "
			+ "queue reached the threshold. Consider adjusting the queue size!";
	public static final String JOB_TIME_ADJUSTED_LOG = "Job {} has adjusted time frames. New job start: {}, new job edn: {}";
	public static final String INCREASE_DEADLINE_WEIGHT_LOG = "Deadline weight priority increased from {} to: {}";
	public static final String INCREASE_POWER_WEIGHT_LOG = "Power weight priority increased from {} to: {}";
}
