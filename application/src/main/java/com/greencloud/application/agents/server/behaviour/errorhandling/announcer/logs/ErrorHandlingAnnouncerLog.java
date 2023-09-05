package com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs;

/**
 * Class contains all constants used in logging information in announcer behaviours for power shortage
 */
public class ErrorHandlingAnnouncerLog {

	// INTERNAL SERVER ERROR START LOG MESSAGES
	public static final String INTERNAL_SERVER_ERROR_START_DETECTED_LOG =
			"Internal server error was detected for server! Power will be cut off at: {}";
	public static final String INTERNAL_SERVER_ERROR_START_NO_IMPACT_LOG =
			"Internal server error won't affect any jobs";
	public static final String INTERNAL_SERVER_ERROR_START_TRANSFER_REQUEST_LOG =
			"Requesting job {} transfer in Cloud Network";

	// POWER SHORTAGE FINISH LOG MESSAGES
	public static final String INTERNAL_SERVER_ERROR_FINISH_DETECTED_LOG =
			"Internal server error has finished! Supplying jobs with green power";
	public static final String INTERNAL_SERVER_ERROR_FINISH_UPDATE_SERVER_LOG =
			"There are no jobs supplied using back up power. Updating server state.";
	public static final String INTERNAL_SERVER_ERROR_FINISH_UPDATE_JOB_STATUS_LOG =
			"Changing the statuses of the jobs and informing the CNA and Green Sources";
	public static final String INTERNAL_SERVER_ERROR_FINISH_USE_GREEN_ENERGY_LOG =
			"Processing job {} with green source energy!";
	public static final String INTERNAL_SERVER_ERROR_FINISH_LEAVE_ON_HOLD_LOG =
			"There are not enough resources to continue the job processing! Leaving job {} on hold";

}
