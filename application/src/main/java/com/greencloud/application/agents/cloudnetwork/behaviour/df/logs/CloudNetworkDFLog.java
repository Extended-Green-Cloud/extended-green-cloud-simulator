package com.greencloud.application.agents.cloudnetwork.behaviour.df.logs;

/**
 * Class contains all constants used in logging information in cloud network DF behaviours
 */
public class CloudNetworkDFLog {

	// FIND SERVERS AND SCHEDULER LOG MESSAGES
	public static final String NO_SERVERS_FOUND_LOG = "No Server Agents were found";
	public static final String NO_SCHEDULER_FOUND_LOG = "Scheduler was not found";

	// NETWORK CHANGE LISTENER
	public static final String FOUND_NEW_SERVERS_LOG = "Found {} new servers in the network!";
	public static final String FOUND_REMOVED_SERVERS_LOG = "Found {} removed servers in the network!";

	// ASK FOR SERVER POWER LOG MESSAGES
	public static final String UPDATE_MAX_CAPACITY_LOG = "Updating information regarding maximum capacity!";
}
