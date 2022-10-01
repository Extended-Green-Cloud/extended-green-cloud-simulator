package com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs;

/**
 * Class contains all constants used in logging information in listener behaviours for power shortage in green source
 */
public class PowerShortageSourceListenerLog {

	// POWER SHORTAGE IN SERVER RECEIVED START INFORMATION LOG MESSAGES
	public static final String SERVER_POWER_SHORTAGE_START_LOG =
			"Received information about job {} power shortage in server. Updating green source state";
	public static final String SERVER_POWER_SHORTAGE_START_NOT_FOUND_LOG =
			"Job {} to divide due to power shortage was not found";

	// POWER SHORTAGE IN SERVER RECEIVED FINISH INFORMATION LOG MESSAGES
	public static final String SERVER_POWER_SHORTAGE_FINISH_CHANGE_LOG =
			"Power shortage in server finished. Changing the status of the power job {}";
	public static final String SERVER_POWER_SHORTAGE_FINISH_NOT_FOUND_LOG =
			"Job {} to supply with green power was not found";

	//POWER SHORTAGE IN SERVER TRANSFER FAILURE LOG MESSAGES
	public static final String SERVER_POWER_SHORTAGE_FAILURE_PUT_ON_HOLD_LOG =
			"Received information about job {} transfer failure. Putting job on hold";
	public static final String SERVER_POWER_SHORTAGE_FAILURE_NOT_FOUND_LOG =
			"Job {} to put on hold was not found";

}
