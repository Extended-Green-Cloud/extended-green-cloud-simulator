package com.greencloud.application.agents.server.behaviour.powershortage.handler.logs;

/**
 * Class contains all constants used in logging information in handler behaviours for power shortage
 */
public class PowerShortageServerHandlerLog {

	// POWER SHORTAGE IN SERVER HANDLING LOG MESSAGES
	public static final String POWER_SHORTAGE_HANDLE_JOB_ON_BACKUP_LOG = "Supplying job with id {} using backup power";
	public static final String POWER_SHORTAGE_HANDLE_JOB_ON_HOLD_LOG = "Putting job with id {} on hold";
	public static final String POWER_SHORTAGE_HANDLE_JOB_ON_HOLD_TEMPORARY_LOG =
			"Job with id {} put temporarily on hold while looking for its transfer";

	// POWER SHORTAGE IN GREEN SOURCE HANDLING JOB TRANSFER LOG MESSAGES
	public static final String GS_TRANSFER_EXECUTION_LOG = "Transferring job between green sources";

}
