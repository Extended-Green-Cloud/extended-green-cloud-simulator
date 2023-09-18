package org.greencloud.commons.utils.messaging.constants;

/**
 * Class stores constants passed as message content during power shortage handling process
 */
public class MessageContentConstants {

	/**
	 * Power shortage content constants
	 *
	 * <p> JOB_NOT_FOUND_CAUSE_MESSAGE 			 		- message content informing that the job was not found in agent </p>
	 * <p> DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE	- message content informing that the job was delayed and
	 * 													  should be already finished </p>
	 * <p> NO_SERVER_AVAILABLE_CAUSE_MESSAGE 	 		- message content informing that there are no servers available
	 * 													  to negotiate the job transfer </p>
	 * <p> SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE 		- message content informing that the internal failure
	 * 													  has occurred in the server </p>
	 * <p> NO_SOURCES_AVAILABLE_CAUSE_MESSAGE	 		- message content informing that there are no green sources
	 * 													  available for the job transfer negotiation </p>
	 * <p> WEATHER_UNAVAILABLE_CAUSE_MESSAGE	 		- message content informing that the weather data could not
	 * 													  be retrieved </p>
	 * <p> NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE	 		- message content informing that there is not enough
	 * 													  green power to execute given job </p>
	 * <p> TRANSFER_SUCCESSFUL_MESSAGE			 		- message content informing that the job transfer has been
	 * 													  established successfully </p>
	 * <p> RE_SUPPLY_SUCCESSFUL_MESSAGE			 		- message content informing that re-supplying given job with
	 * 													  green power was established successfully </p>
	 */
	public static final String JOB_NOT_FOUND_CAUSE_MESSAGE = "JOB_NOT_FOUND_CAUSE_MESSAGE";
	public static final String DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE = "DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE";
	public static final String NO_SERVER_AVAILABLE_CAUSE_MESSAGE = "NO_SERVER_AVAILABLE_CAUSE_MESSAGE";
	public static final String SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE = "SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE";
	public static final String NO_SOURCES_AVAILABLE_CAUSE_MESSAGE = "NO_SOURCES_AVAILABLE_CAUSE_MESSAGE";
	public static final String WEATHER_UNAVAILABLE_CAUSE_MESSAGE = "WEATHER_UNAVAILABLE_CAUSE_MESSAGE";
	public static final String NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE = "NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE";
	public static final String TRANSFER_SUCCESSFUL_MESSAGE = "TRANSFER_SUCCESSFUL_MESSAGE";
	public static final String RE_SUPPLY_SUCCESSFUL_MESSAGE = "RE_SUPPLY_SUCCESSFUL_MESSAGE";
}
