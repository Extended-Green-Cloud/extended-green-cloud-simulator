package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours during job scheduling process
 */
public class JobSchedulingInitiatorLog {

	// CNA LOOKUP LOG MESSAGES
	public static final String NO_CLOUD_RESPONSES_LOG = "No responses were retrieved";
	public static final String NO_CLOUD_AVAILABLE_RETRY_LOG =
			"All Cloud Network Agents refused to the call for proposal. Job postponed and scheduled for next execution.";
	public static final String NO_CLOUD_AVAILABLE_NO_RETRY_LOG =
			"All Cloud Network Agents refused to the call for proposal. Sending failure information";
	public static final String INVALID_CLOUD_PROPOSAL_LOG =
			"I didn't understand any proposal from Cloud Network Agents";
	public static final String SEND_ACCEPT_TO_CLOUD_LOG = "Sending ACCEPT_PROPOSAL to {}";

}
