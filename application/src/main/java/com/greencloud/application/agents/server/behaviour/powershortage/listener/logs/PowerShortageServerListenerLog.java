package com.greencloud.application.agents.server.behaviour.powershortage.listener.logs;

/**
 * Class contains all constants used in logging information in listener behaviours for power shortage
 */
public class PowerShortageServerListenerLog {

	// POWER SHORTAGE IN GREEN SOURCE RECEIVED REQUEST LOG MESSAGES
	public static final String GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG =
			"Sending call for proposal to Green Source Agents to transfer job with id {}";
	public static final String GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG =
			"No green sources available. Sending transfer request to cloud network";

	// POWER SHORTAGE IN GREEN SOURCE TRANSFER CONFIRMATION LOG MESSAGES
	public static final String GS_TRANSFER_JOB_FINISHED_LOG = "Job execution finished before transfer";
	public static final String GS_TRANSFER_FAILED_LOG = "Job {} transfer has failed in green source. Passing transfer request to Cloud Network";
	public static final String GS_TRANSFER_CONFIRMED_LOG =
			"Scheduling the job {} transfer. Sending confirmation to green source";

	// POWER SHORTAGE IN GREEN SOURCE FINISHED LOG MESSAGES
	public static final String GS_SHORTAGE_FINISH_LOG =
			"Received the information that the power shortage is finished. Supplying job {} with green energy";

}
