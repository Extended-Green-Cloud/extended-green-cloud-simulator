package com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs;

/**
 * Class contains all constants dedicated to logging information in handler behaviours used during handling executed jobs
 */
public class JobHandlingHandlerLog {

	// JOB START LOG MESSAGES
	public static final String JOB_START_NO_GREEN_SOURCE_LOG = "Job execution couldn't start: there is no green source for the job {}";
	public static final String JOB_START_NO_PRESENT_LOG = "Job execution couldn't start: job {} is not present";
	public static final String JOB_START_LOG = "Start executing the job for {}";
	public static final String JOB_START_NO_INFORM_LOG = "Start executing the job for {} without informing CNA";

	// JOB FINISH LOG MESSAGES
	public static final String JOB_FINISH_LOG = "Finished executing the job {} at {}";
}
