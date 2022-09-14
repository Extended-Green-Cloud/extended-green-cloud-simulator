package com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.listener.logs;

/**
 * Class contains all constants used in logging information in listener behaviours for power shortage in cloud network
 */
public class PowerShortageCloudListenerLog {

	// POWER SHORTAGE IN SERVER TRANSFER REQUEST LOG MESSAGES
	public static final String SERVER_TRANSFER_REQUEST_ASK_SERVERS_LOG =
			"Sending call for proposal to Server Agents to transfer job with id {}";
	public static final String SERVER_TRANSFER_REQUEST_NO_SERVERS_AVAILABLE_LOG =
			"No servers available. Passing the information to client and server";
	public static final String SERVER_TRANSFER_REQUEST_JOB_NOT_FOUND_LOG =
			"Job {} for transfer was not found in cloud network";

}
