package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.logs;

/**
 * Class contains all constants used in logging information in handler behaviours during job handling process
 */
public class JobHandlingHandlerLog {

	// DELAYED JOB HANDLING LOG MESSAGES
	public static final String JOB_DELAY_LOG =
			"There is no message regarding the job start. Sending request to the server";

	// RETRY JOB REQUEST LOG MESSAGES
	public static final String TRIGGER_RETRY_LOG = "Retrying to process a job with id {}";
}
