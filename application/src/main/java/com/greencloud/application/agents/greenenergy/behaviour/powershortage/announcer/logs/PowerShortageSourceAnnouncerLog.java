package com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs;

/**
 * Class contains all constants used in logging information in announcer behaviours for power shortage in green source
 */
public class PowerShortageSourceAnnouncerLog {

	// POWER SHORTAGE IN SOURCE STARTED LOG MESSAGES
	public static final String POWER_SHORTAGE_SOURCE_START_LOG =
			"Power shortage was detected! Power shortage will happen at: {}";
	public static final String POWER_SHORTAGE_SOURCE_START_WEATHER_LOG =
			"Weather-caused power shortage was detected! Power shortage will happen at: {}";
	public static final String POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG =
			"Power shortage won't affect any jobs";
	public static final String POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG = "Requesting job {} transfer in parent Server";

	// POWER SHORTAGE IN SOURCE FINISHED LOG MESSAGES
	public static final String POWER_SHORTAGE_SOURCE_FINISH_LOG =
			"Power shortage has finished!!! Supplying jobs with green power";
	public static final String POWER_SHORTAGE_SOURCE_FINISH_NO_JOBS_LOG =
			"There are no jobs which were on hold. Updating the maximum power";
	public static final String POWER_SHORTAGE_SOURCE_VERIFY_POWER_LOG =
			"Checking if the job {} can be put in progress";
	public static final String POWER_SHORTAGE_SOURCE_JOB_ENDED_LOG =
			"Job {} has ended before supplying it back with green power";

}
