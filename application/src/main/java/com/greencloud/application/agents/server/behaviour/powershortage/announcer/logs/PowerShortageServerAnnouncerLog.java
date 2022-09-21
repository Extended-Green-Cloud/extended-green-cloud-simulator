package com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs;

/**
 * Class contains all constants used in logging information in announcer behaviours for power shortage
 */
public class PowerShortageServerAnnouncerLog {

	// POWER SHORTAGE START LOG MESSAGES
	public static final String POWER_SHORTAGE_START_DETECTED_LOG =
			"Power shortage was detected for server! Power shortage will begin at: {}";
	public static final String POWER_SHORTAGE_START_NO_IMPACT_LOG = "Power shortage won't affect any jobs";
	public static final String POWER_SHORTAGE_START_TRANSFER_REQUEST_LOG =
			"Requesting job {} transfer in Cloud Network";

	// POWER SHORTAGE FINISH LOG MESSAGES
	public static final String POWER_SHORTAGE_FINISH_DETECTED_LOG =
			"Power shortage has finished! Supplying jobs with green power";
	public static final String POWER_SHORTAGE_FINISH_UPDATE_CAPACITY_LOG =
			"There are no jobs supplied using back up power. Updating the maximum power";
	public static final String POWER_SHORTAGE_FINISH_UPDATE_JOB_STATUS_LOG =
			"Changing the statuses of the jobs and informing the CNA and Green Sources";
	public static final String POWER_SHORTAGE_FINISH_USE_BACK_UP_LOG = "Supporting job {} with back-up power!";
	public static final String POWER_SHORTAGE_FINISH_USE_GREEN_ENERGY_LOG =
			"Supporting job {} with green source power!";
	public static final String POWER_SHORTAGE_FINISH_LEAVE_ON_HOLD_LOG =
			"There is not enough available power to supply job with back up power! Leaving job {} on hold";

}
