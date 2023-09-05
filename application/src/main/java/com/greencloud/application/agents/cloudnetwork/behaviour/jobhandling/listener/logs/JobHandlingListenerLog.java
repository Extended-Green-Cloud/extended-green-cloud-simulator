package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs;

/**
 * Class contains all constants used in logging information in Cloud Network listener behaviours
 * during job handling process
 */
public class JobHandlingListenerLog {

	// CLIENTS JOB LISTENER LOG MESSAGES
	public static final String SEND_CFP_NEW_LOG =
			"Sending call for proposal to Server Agents for a job request with jobId {}!";
	public static final String NEW_JOB_NO_SERVERS_LOG =
			"There are no active servers in given CNA to handle job execution! Sending refuse.";

	// JOB STATUS CHANGE LOG MESSAGES
	public static final String JOB_CONFIRMED_STATUS_LOG =
			"Job {} has been confirmed as accepted in server";
	public static final String SEND_GREEN_POWER_STATUS_LOG =
			"Sending information that the job {} is executed again using green power";
	public static final String SEND_BACK_UP_STATUS_LOG =
			"Sending information that the job {} is executed using back-up power";
	public static final String SEND_ON_HOLD_STATUS_LOG =
			"Sending information that the job {} has been put on hold";
	public static final String SEND_JOB_START_STATUS_LOG =
			"Sending information that the job {} execution has started";
	public static final String SEND_JOB_FINISH_STATUS_LOG =
			"Sending information that the job {} execution has finished";
	public static final String SEND_JOB_FAILED_STATUS_LOG =
			"Sending information that the job {} execution has failed";
}
