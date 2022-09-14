package com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours during job announcement process
 */
public class JobAnnouncementInitiatorLog {

	// NEW JOB ANNOUNCEMENT LOG MESSAGES
	public static final String SEND_CFP_TO_CLOUD_LOG =
			"Sending call for proposal to Cloud Network Agents with job request.";
	public static final String NO_CLOUD_RESPONSES_LOG = "No responses were retrieved";
	public static final String NO_CLOUD_AVAILABLE_RETRY_LOG =
			"All Cloud Network Agents refused to the call for proposal - will retry for {} time";
	public static final String NO_CLOUD_AVAILABLE_NO_RETRY_LOG =
			"All Cloud Network Agents refused to the call for proposal";
	public static final String INVALID_CLOUD_PROPOSAL_LOG =
			"I didn't understand any proposal from Cloud Network Agents";
	public static final String SEND_ACCEPT_TO_CLOUD_LOG = "Sending ACCEPT_PROPOSAL to {}";

}
