package com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs;

/**
 * Class contains all constants dedicated to logging information in initiator behaviours used during handling incoming jobs
 */
public class JobHandlingInitiatorLog {

	// LOOKING FOR AVAILABLE GREEN SOURCE SUPPLIER LOG MESSAGES
	public static final String NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG = "No responses were retrieved from green sources";
	public static final String NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG =
			"No Green Sources available - sending refuse message to Cloud Network Agent";
	public static final String NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG =
			"No enough capacity - sending refuse message to Cloud Network Agent";
	public static final String NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG =
			"Chosen Green Source for the job with id {} : {}. Sending job volunteering offer to Cloud Network Agent";

	// MAKING AND OFFER TO EXECUTE GIVEN JOB LOG MESSAGES
	public static final String SERVER_OFFER_ACCEPT_PROPOSAL_FAILURE_LOG = "Not enough power to execute the job {}. Sending failure information and rejecting green source proposal";
	public static final String SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG = "Sending ACCEPT_PROPOSAL to Green Source Agent";
	public static final String SERVER_OFFER_REJECT_LOG = "Cloud Network {} rejected the job volunteering offer";

}
