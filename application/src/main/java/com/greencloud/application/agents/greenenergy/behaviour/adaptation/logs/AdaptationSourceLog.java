package com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs;

/**
 * Class contains all constants used in logging information in adaptation behaviours of the given Green Source agent
 */
public class AdaptationSourceLog {

	// INITIATE SERVER CONNECTION LOG MESSAGES
	public static final String CONNECTION_FAILED_LOG =
			"Connection failed - Server {} is already connected to the Green Source.";
	public static final String CONNECTION_SUCCEEDED_LOG = "Green Source successfully connected with a Server {}.";

	// INITIATE GREEN SOURCE DEACTIVATION LOG MESSAGES
	public static final String DEACTIVATION_FAILED_LOG =
			"Deactivation failed - Server {} is not connected with a given Green Source.";
	public static final String DEACTIVATION_SUCCEEDED_LOG = "Green Source was successfully deactivated in a Server {}.";
	public static final String DEACTIVATION_FINISH_REMAIN_JOBS_LOG =
			"There are {} Server jobs left. Green Source will wait for remaining jobs finish.";
	public static final String INITIATE_GREEN_SOURCE_DISCONNECTION_LOG =
			"There are no Server jobs left. Initiating Green Source disconnection";

	// INITIATE GREEN SOURCE DISCONNECTION LOG MESSAGES
	public static final String DISCONNECTION_FAILED_LOG =
			"Disconnection failed - Server {} couldn't disconnect a Green Source.";
	public static final String DISCONNECTION_SUCCEEDED_LOG =
			"Green Source was successfully disconnected from Server {}.";
}
