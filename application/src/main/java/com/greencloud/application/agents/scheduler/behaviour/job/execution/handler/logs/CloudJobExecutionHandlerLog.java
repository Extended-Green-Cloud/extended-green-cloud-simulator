package com.greencloud.application.agents.scheduler.behaviour.job.execution.handler.logs;

/**
 * Class contains all constants used in logging information in handler behaviours during job execution in cloud
 */
public class CloudJobExecutionHandlerLog {

	// JOB START IN CLOUD LOG MESSAGES
	public static final String JOB_CLOUD_START_NO_PRESENT_LOG = "Job execution couldn't start: job {} is not present in cloud";
	public static final String JOB_CLOUD_START_LOG = "Start executing the job {} in cloud.";

	// JOB FINISH IN CLOUD LOG MESSAGES
	public static final String JOB_CLOUD_FINISH_LOG = "Finished executing the job {} at {} in cloud.";
}
