package com.greencloud.application.messages.domain.constants;

/**
 * Class stores constants passed as message content during power shortage handling process
 */
public class PowerShortageMessageContentConstants {

	/**
	 * JOB_NOT_FOUND_CAUSE_MESSAGE 			 		- message content informing that the job was not found in agent
	 * DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE	- message content informing that the job was delayed and should be already finished
	 * NO_SERVER_AVAILABLE_CAUSE_MESSAGE 	 		- message content informing that there are no servers available to negotiate the job transfer
	 * SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE 		- message content informing that the internal failure has occurred in the server
	 * NO_SOURCES_AVAILABLE_CAUSE_MESSAGE	 		- message content informing that there are no green sources available for the job transfer negotiation
	 * WEATHER_UNAVAILABLE_CAUSE_MESSAGE	 		- message content informing that the weather data could not be retrieved
	 * NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE	 		- message content informing that there is not enough green power to execute given job
	 * TRANSFER_SUCCESSFUL_MESSAGE			 		- message content informing that the job transfer has been established successfully
	 * TRANSFER_PROCESSING_MESSAGE			 		- message content informing that the job transfer is being processed
	 * PROCESSING_RE_SUPPLY_MESSAGE			 		- message content informing that the green source is processing a request to re-supply given job with green energy
	 * RE_SUPPLY_SUCCESSFUL_MESSAGE			 		- message content informing that re-supplying given job with green power was established successfully
	 */
	public static final String JOB_NOT_FOUND_CAUSE_MESSAGE = "JOB_NOT_FOUND_CAUSE_MESSAGE";
	public static final String DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE = "DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE";
	public static final String NO_SERVER_AVAILABLE_CAUSE_MESSAGE = "NO_SERVER_AVAILABLE_CAUSE_MESSAGE";
	public static final String SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE = "SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE";
	public static final String NO_SOURCES_AVAILABLE_CAUSE_MESSAGE = "NO_SOURCES_AVAILABLE_CAUSE_MESSAGE";
	public static final String WEATHER_UNAVAILABLE_CAUSE_MESSAGE = "WEATHER_UNAVAILABLE_CAUSE_MESSAGE";
	public static final String NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE = "NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE";
	public static final String TRANSFER_SUCCESSFUL_MESSAGE = "TRANSFER_SUCCESSFUL_MESSAGE";
	public static final String TRANSFER_PROCESSING_MESSAGE = "TRANSFER_PROCESSING_MESSAGE";
	public static final String PROCESSING_RE_SUPPLY_MESSAGE = "PROCESSING_RE_SUPPLY_MESSAGE";
	public static final String RE_SUPPLY_SUCCESSFUL_MESSAGE = "RE_SUPPLY_SUCCESSFUL_MESSAGE";
}
