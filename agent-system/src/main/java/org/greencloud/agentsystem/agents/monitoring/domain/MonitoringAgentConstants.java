package org.greencloud.agentsystem.agents.monitoring.domain;

/**
 * Class stores all predefined constants for Monitoring Agent.
 */
public class MonitoringAgentConstants {

	/**
	 * Maximal number of messages asking for the weather data that can be read at once.
	 */
	public static final int MAX_NUMBER_OF_REQUESTS = 100;
	/**
	 * Maximal number of weather requests processed in batch.
	 */
	public static final int WEATHER_REQUESTS_IN_BATCH = 10;
}
