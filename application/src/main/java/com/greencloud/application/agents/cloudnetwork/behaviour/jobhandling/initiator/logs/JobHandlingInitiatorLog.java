package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs;

/**
 * Class contains all constants used in logging information in Cloud Network initiator
 * behaviours during job handling process
 */
public class JobHandlingInitiatorLog {

	// JOB START CHECK LOG MESSAGES
	public static final String JOB_HAS_STARTED_LOG =
			"Received job started confirmation. Sending information that the job {} execution has started";
	public static final String JOB_HAS_NOT_STARTED_LOG =
			"The job {} execution hasn't started yet. Sending delay information to client";
	public static final String JOB_HAS_FAILED_LOG =
			"The job {} execution has failed in the meantime. Sending failure information to client";

	// FINDING JOB EXECUTOR LOG MESSAGES
	public static final String NO_SERVER_RESPONSES_LOG = "No responses from servers were retrieved";
	public static final String NO_SERVER_AVAILABLE_LOG =
			"All servers refused to execute the job - sending refuse response to Scheduler";
	public static final String CHOSEN_SERVER_FOR_JOB_LOG =
			"Chosen Server for the job {}: {}. Sending job execution offer to Scheduler Agent";

	// JOB OFFER MAKING LOG MESSAGES
	public static final String ACCEPT_SERVER_PROPOSAL_LOG = "Sending ACCEPT_PROPOSAL to Server Agent";
	public static final String REJECT_SERVER_PROPOSAL_LOG = "Scheduler {} rejected the job proposal";

	// JOB CANCELLING LOG MESSAGES
	public static final String CANCEL_JOB_IN_CNA = "Cancelling job part {}!";
	public static final String CANCEL_JOB_IN_CNA_NOT_FOUND = "Job {} for cancellation was not found in CNA!";
	public static final String CANCEL_JOB_ALL_RESPONSES =
			"All responses from Servers received. Remaining jobs that were not cancelled: {}!";

}
