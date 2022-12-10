package com.greencloud.application.agents.server.behaviour.df.logs;

/**
 * Class contains all constants used in logging information in server DF behaviours
 */
public class ServerDFLog {

	// SUBSCRIBE GREEN SOURCE SERVICE LOG MESSAGES
	public static final String SUBSCRIBE_GS_SERVICE_LOG = "Subscribing to Green Source Service";
	public static final String RECEIVE_GS_ANNOUNCEMENT_LOG = "Received message that new Green Source registered its service";

	// LISTEN FOR ADDITIONAL GREEN SOURCE LOG MESSAGES
	public static final String GREEN_SOURCE_ALREADY_CONNECTED_LOG = "Green Source {} is already connected to the given server.";
	public static final String CONNECT_GREEN_SOURCE_LOG = "Connecting Green Source {} to the server";
}
