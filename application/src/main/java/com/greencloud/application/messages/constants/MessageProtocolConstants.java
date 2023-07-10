package com.greencloud.application.messages.constants;

/**
 * Class stores constants describing the message protocols
 */
public class MessageProtocolConstants {

	/**
	 * Service protocols
	 *
	 * <p> ASK_FOR_POWER_PROTOCOL 		- protocol used in messages sent by the Cloud Network Agents asking owned Servers for
	 * 									  their maximum capacity information </p>
	 * <p> ASK_FOR_CONTAINER_PROTOCOL	- protocol used in messages sent to servers to obtain their container allocation </p>
	 */
	public static final String ASK_FOR_POWER_PROTOCOL= "ASK_FOR_POWER_PROTOCOL";
	public static final String ASK_FOR_CONTAINER_PROTOCOL= "ASK_FOR_CONTAINER_PROTOCOL";

	/**
	 * Agent connections protocols
	 *
	 * <p> CONNECT_GREEN_SOURCE_PROTOCOL 	- protocol used in messages sent by the Green Source informing Server that
	 * 								   	  	  it can be added to its connections </p>
	 * <p> DEACTIVATE_GREEN_SOURCE_PROTOCOL	- protocol used in messages sent by the Green Source informing Server that
	 * 								   	  	  it should deactivate given green source </p>
	 * <p> DISCONNECT_GREEN_SOURCE_PROTOCOL	- protocol used in messages sent by the Green Source informing Server that
	 * 								   	  	  it remove given green source connection </p>
	 * <p> DISABLE_SERVER_PROTOCOL	  		- protocol used in messages sent by the Server informing CNA that
	 *  								  	  the given Server is disabled </p>
	 * <p> DISABLE_SERVER_PROTOCOL	  		- protocol used in messages sent by the Server informing CNA that
	 * 	  								  	  the given Server is enabled </p>
	 */
	public static final String CONNECT_GREEN_SOURCE_PROTOCOL = "CONNECT_GREEN_SOURCE_PROTOCOL";
	public static final String DEACTIVATE_GREEN_SOURCE_PROTOCOL = "DEACTIVATE_GREEN_SOURCE_PROTOCOL";
	public static final String DISCONNECT_GREEN_SOURCE_PROTOCOL = "DISCONNECT_GREEN_SOURCE_PROTOCOL";
	public static final String DISABLE_SERVER_PROTOCOL = "DISABLE_SERVER_PROTOCOL";
	public static final String ENABLE_SERVER_PROTOCOL = "ENABLE_SERVER_PROTOCOL";

	/**
	 * Call for proposals protocols
	 *
	 * <p> SCHEDULER_JOB_CFP_PROTOCOL        - protocol used in scheduler's call for proposal message that is
	 * 											sent to cloud networks while looking for the network for job execution </p>
	 * <p> CNA_JOB_CFP_PROTOCOL              - protocol used in cloud network's call for proposal message that is sent
	 * 										    to the servers while looking for the server which will execute the job </p>
	 * <p> SERVER_JOB_CFP_PROTOCOL           - protocol used in the server's call for proposal message that is sent to the green
	 * 										   sources while looking for the green source which will supply the
	 * 										   server with power needed for the job execution </p>
	 */
	public static final String SCHEDULER_JOB_CFP_PROTOCOL = "SCHEDULER_JOB_CFP";
	public static final String CNA_JOB_CFP_PROTOCOL = "CNA_JOB_CFP";
	public static final String SERVER_JOB_CFP_PROTOCOL = "SERVER_JOB_CFP";

	/**
	 * Job state protocols
	 *
	 * <p> ANNOUNCED_JOB_PROTOCOL		   - protocol used in messages informing that the job has been announced in network </p>
	 * <p> MANUAL_JOB_FINISH_PROTOCOL      - protocol used in messages informing that the job had to be finished manually because the time </p>
	 * 										 after which the information about job being finished has passed </p>
	 * <p> JOB_START_STATUS_PROTOCOL       - protocol used in messages with request of the update regarding the job start status </p>
	 * <p> CHANGE_JOB_STATUS_PROTOCOL      - protocol used in messages informing that the status of the job has changed </p>
	 * <p> CONFIRMED_TRANSFER_PROTOCOL	   - protocol used in messages informing that the job transfer was confirmed </p>
	 * <p> FAILED_JOB_PROTOCOL         	   - protocol used in messages informing that the job execution has failed </p>
	 * <p> FAILED_TRANSFER_PROTOCOL        - protocol used in messages informing that the job transfer has failed </p>
	 * <p> FAILED_SOURCE_TRANSFER_PROTOCOL - protocol used in messages informing that the job transfer between green sources has failed </p>
	 * <p> CANCEL_JOB_PROTOCOL			   - protocol used in messages informing about job cancellation </p>
	 */
	public static final String ANNOUNCED_JOB_PROTOCOL = "ANNOUNCED";
	public static final String MANUAL_JOB_FINISH_PROTOCOL = "MANUAL_FINISH";
	public static final String JOB_START_STATUS_PROTOCOL = "START_STATUS_PROTOCOL";
	public static final String CHANGE_JOB_STATUS_PROTOCOL = "CHANGE_JOB_STATUS_PROTOCOL";
	public static final String CONFIRMED_TRANSFER_PROTOCOL = "CONFIRMED_TRANSFER_PROTOCOL";
	public static final String FAILED_JOB_PROTOCOL = "FAILED_JOB_PROTOCOL";
	public static final String FAILED_TRANSFER_PROTOCOL = "FAILED_TRANSFER_PROTOCOL";
	public static final String FAILED_SOURCE_TRANSFER_PROTOCOL = "FAILED_SOURCE_TRANSFER_PROTOCOL";
	public static final String CANCEL_JOB_PROTOCOL = "CANCEL_JOB_PROTOCOL";


	/**
	 * Power shortage protocols
	 *
	 * <p> POWER_SHORTAGE_ALERT_PROTOCOL                - protocol used in messages informing the agent that the power
	 * 													  shortage was detected </p>
	 * <p> POWER_SHORTAGE_FINISH_ALERT_PROTOCOL         - protocol used in messages informing that the power shortage
	 * 												      for given agent has finished </p>
	 * <p> SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL       - protocol used in messages informing that the job should
	 * 													  be put on hold in green source due to server power shortage </p>
	 * <p> SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL     - protocol used in messages informing the server want to verify
	 * 												      if given job can be supplied again using green energy </p>
	 * <p> POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL     - protocol used in messages confirming that new green source
	 * 													  will supplied affected job with green power </p>
	 * <p> SERVER_POWER_SHORTAGE_ALERT_PROTOCOL         - protocol used in informing that the server is affected by
	 * 													  the power shortage </p>
	 * <p> POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL       - protocol informing that the job is being transferred on servers'
	 * 													  level (between servers from given CNA, not between
	 * 													  green sources from given Server) </p>
	 */
	public static final String POWER_SHORTAGE_ALERT_PROTOCOL = "POWER_SHORTAGE_ALERT_PROTOCOL";
	public static final String POWER_SHORTAGE_FINISH_ALERT_PROTOCOL = "POWER_SHORTAGE_FINISH_ALERT_PROTOCOL";
	public static final String SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL = "SERVER_POWER_SHORTAGE_ON_HOLD";
	public static final String SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL = "SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL";
	public static final String POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL = "POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL";
	public static final String SERVER_POWER_SHORTAGE_ALERT_PROTOCOL = "SERVER_POWER_SHORTAGE_ALERT_PROTOCOL";
	public static final String POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL = "POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL";

	/**
	 * Dynamic weather handling related protocols
	 *
	 * <p> ON_HOLD_JOB_CHECK_PROTOCOL           - protocol used in messages that check the weather before putting
	 * 											  the job which was on hold back to in progress </p>
	 * <p> PERIODIC_WEATHER_CHECK_PROTOCOL      - protocol used in messages that check periodically the current weather </p>
	 */
	public static final String ON_HOLD_JOB_CHECK_PROTOCOL = "ON_HOLD_JOB_CHECK_PROTOCOL";
	public static final String PERIODIC_WEATHER_CHECK_PROTOCOL = "PERIODIC_WEATHER_CHECK_PROTOCOL";

}
