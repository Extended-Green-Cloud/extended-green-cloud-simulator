package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs;

/**
 * Class contains all constants dedicated to logging messages in power supply listener behaviours
 */
public class PowerSupplyListenerLog {

	// POWER SUPPLY REQUEST LOG MESSAGE
	public static final String INCORRECT_WEATHER_DATA_FORMAT_LOG =
			"I didn't understand the response with the weather data, sending refuse message to server";
	public static final String NO_ENERGY_LOG =
			"There is not enough energy to provide for job {}. Sending refuse message.";
	public static final String WEATHER_UNAVAILABLE_FOR_JOB_LOG =
			"Weather data not available, sending refuse message to server.";
	public static final String TOO_BAD_WEATHER_LOG =
			"Too bad weather conditions, sending refuse message to server for job with id {}.";
	public static final String NOT_ENOUGH_POWER_LOG =
			"Refusing job with id {} - not enough available power. Needed {}, available {}";
	public static final String POWER_SUPPLY_PROPOSAL_LOG =
			"Replying with propose message to server for job with id {}.";

	// POWER SUPPLY STATUS LOG MESSAGES
	public static final String FINISH_POWER_SUPPLY_LOG = "Finish the execution of the job {}";
	public static final String START_POWER_SUPPLY_LOG = "Started the execution of the job with id {}";

	public static final String JOB_NOT_FOUND = "Job {} was not found.";
}
