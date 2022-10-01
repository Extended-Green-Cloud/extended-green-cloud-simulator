package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours during job handling process
 */
public class JobHandlingInitiatorLog {

	// JOB START CHECK LOG MESSAGES
	public static final String JOB_STATUS_IS_CHECKED_LOG =
			"Job {} status is being checked by the server";
	public static final String JOB_HAS_STARTED_LOG =
			"Received job started confirmation. Sending information that the job {} execution has started";
	public static final String JOB_HAS_NOT_STARTED_LOG =
			"The job {} execution hasn't started yet. Sending delay information to client";

	// FINDING JOB EXECUTOR LOG MESSAGES
	public static final String NO_SERVER_RESPONSES_LOG = "No responses were retrieved";
	public static final String NO_SERVERS_AVAILABLE_RETRIES_LIMIT_LOG =
			"No Servers available - reached limit of retires - sending refuse message to client";
	public static final String NO_SERVERS_AVAILABLE_RETRY_LOG =
			"No Servers available - schedule {} retry to process the message";
	public static final String CHOSEN_SERVER_FOR_JOB_LOG =
			"Chosen Server for the job {}: {}. Sending job execution offer to Client";
	public static final String INCORRECT_PROPOSAL_FORMAT_LOG =
			"I didn't understand any proposal from Server Agents";

	// JOB OFFER MAKING LOG MESSAGES
	public static final String ACCEPT_SERVER_PROPOSAL_LOG = "Sending ACCEPT_PROPOSAL to Server Agent";
	public static final String REJECT_SERVER_PROPOSAL_LOG = "Client {} rejected the job proposal";

}
