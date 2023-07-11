package com.greencloud.application.agents.server.behaviour.df.listener.logs;

/**
 * Class contains all constants used in logging information in server DF listener behaviours
 */
public class ServerDFListenerLog {

	// LISTEN FOR ADDITIONAL GREEN SOURCE LOG MESSAGES
	public static final String GREEN_SOURCE_ALREADY_CONNECTED_LOG =
			"Green Source {} is already connected to the given server.";
	public static final String CONNECT_GREEN_SOURCE_LOG = "Connecting Green Source {} to the server";

	// LISTEN FOR GREEN SOURCE DISCONNECTION LOG MESSAGES
	public static final String GREEN_SOURCE_NOT_CONNECTED_TO_SERVER_LOG =
			"Green Source {} was not found in server connections";
	public static final String DEACTIVATE_GREEN_SOURCE_LOG =
			"Changing status of Green Source {} to inactive";
	public static final String GREEN_SOURCE_NOT_DEACTIVATED_LOG =
			"Green Source {} is still active thus cannot be disconnected.";
	public static final String DISCONNECT_GREEN_SOURCE_LOG =
			"Disconnecting Green Source {} from given server";

	// LISTEN FOR CLOUD NETWORK POWER INFORMATION REQUEST
	public static final String RECEIVED_POWER_INFORMATION_REQUEST_LOG =
			"Cloud Network Agent {} asked server for maximum capacity information.";

	// LISTEN FOR CLOUD NETWORK CONTAINER INFORMATION REQUEST
	public static final String RECEIVED_CONTAINER_INFORMATION_REQUEST_LOG =
			"Cloud Network Agent {} asked server for its container allocation.";

}
