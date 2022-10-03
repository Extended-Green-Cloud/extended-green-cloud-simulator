package com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs;

/**
 * Class contains all constants dedicated to logging information in listener behaviours used during handling incoming jobs
 */
public class JobHandlingListenerLog {

	// RECEIVING NEW JOB REQUEST LOG MESSAGES
	public static final String SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG = "Sending call for proposal to Green Source Agents";
	public static final String SERVER_NEW_JOB_LACK_OF_POWER_LOG =
			"Not enough available power! Sending refuse message to Cloud Network Agent";

	// POWER SUPPLY UPDATE LOG MESSAGES
	public static final String SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG = "Announcing new job {} in network!";
	public static final String SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG = "Scheduling the execution of the job {}";
	public static final String SUPPLY_CONFIRMATION_INFORM_CNA_LOG = "Confirming job {} execution";
	public static final String SUPPLY_CONFIRMATION_INFORM_CNA_TRANSFER_LOG = "Confirming job {} transfer";
	public static final String SUPPLY_CONFIRMATION_JOB_FINISHED_LOG =
			"Job {} must have been finished or transferred before its execution";
	public static final String SUPPLY_FINISHED_MANUALLY_LOG =
			"Information about finishing job with id {} does not reach the green source. Finished executing the job for {}";

	// JOB START STATUS REQUEST LOG MESSAGES
	public static final String JOB_START_STATUS_RECEIVED_REQUEST_LOG = "Received request to verify job start status {}";
}
