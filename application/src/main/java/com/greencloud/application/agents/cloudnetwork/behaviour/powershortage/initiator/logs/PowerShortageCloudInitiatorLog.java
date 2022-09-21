package com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours for power shortage in cloud network
 */
public class PowerShortageCloudInitiatorLog {

	// POWER SHORTAGE IN SERVER JOB TRANSFER LOG MESSAGES
	public static final String SERVER_TRANSFER_NO_RESPONSE_LOG =
			"No responses were retrieved from servers for job transfer";
	public static final String SERVER_TRANSFER_NO_SERVERS_AVAILABLE_LOG =
			"No Servers available - informing server about transfer failure";
	public static final String SERVER_TRANSFER_CHOSEN_SERVER_LOG = "Chosen Server for the job {} transfer: {}";
}
