package com.greencloud.application.messages.domain.constants;

/**
 * Constants describing the message protocols.
 */
public class MessageProtocolConstants {

	/**
	 * Call for proposals protocols
	 * CLIENT_JOB_CFP_PROTOCOL           - protocol used in client's call for proposal message that is sent to cloud networks while looking
	 * for the network for job execution
	 * CNA_JOB_CFP_PROTOCOL              - protocol used in cloud network's call for proposal message that is sent to the servers while
	 * looking for the server which will execute the job
	 * SERVER_JOB_CFP_PROTOCOL           - protocol used in the server's call for proposal message that is sent to the green sources while
	 * looking for the green source which will supply the server with power needed for the job execution
	 */
	public static final String CLIENT_JOB_CFP_PROTOCOL = "CLIENT_JOB_CFP";
	public static final String CNA_JOB_CFP_PROTOCOL = "CNA_JOB_CFP";
	public static final String SERVER_JOB_CFP_PROTOCOL = "SERVER_JOB_CFP";

	/**
	 * Job status protocols
	 * CONFIRMED_JOB_PROTOCOL 	       - protocol used in messages informing that the job has been confirmed as accepted
	 * CONFIRMED_TRANSFER_PROTOCOL     - protocol used in messages informing that the job transfer has been confirmed as accepted
	 * FINISH_JOB_PROTOCOL             - protocol used in messages informing the agent that the job execution has finished
	 * MANUAL_JOB_FINISH_PROTOCOL      - protocol used in messages informing that the job had to be finished manually because the time
	 * after which the information about job being finished has passed
	 * STARTED_JOB_PROTOCOL            - protocol used in messages informing the agent that the job execution has started
	 * JOB_START_STATUS_PROTOCOL       - protocol used in messages with request of the update regarding the job start status
	 * DELAYED_JOB_PROTOCOL            - protocol used in messages informing that the job execution started with the delay
	 * BACK_UP_POWER_JOB_PROTOCOL  	   - protocol used in messages informing that the job is executed using the back-up power
	 * GREEN_POWER_JOB_PROTOCOL    	   - protocol used in messages informing the client that the job is executed using green power
	 * FAILED_JOB_PROTOCOL         	   - protocol used in messages informing that the job execution has failed
	 * FAILED_TRANSFER_PROTOCOL        - protocol used in messages informing that the job transfer has failed
	 * FAILED_SOURCE_TRANSFER_PROTOCOL - protocol used in messages informing that the job transfer between green sources has failed
	 */
	public static final String CONFIRMED_JOB_PROTOCOL = "CONFIRMED";
	public static final String CONFIRMED_TRANSFER_PROTOCOL = "CONFIRMED_TRANSFER";
	public static final String FINISH_JOB_PROTOCOL = "FINISH";
	public static final String MANUAL_JOB_FINISH_PROTOCOL = "MANUAL_FINISH";
	public static final String STARTED_JOB_PROTOCOL = "STARTED";
	public static final String JOB_START_STATUS_PROTOCOL = "START_STATUS_PROTOCOL";
	public static final String DELAYED_JOB_PROTOCOL = "DELAYED";
	public static final String BACK_UP_POWER_JOB_PROTOCOL = "BACK_UP_POWER_JOB_PROTOCOL";
	public static final String GREEN_POWER_JOB_PROTOCOL = "GREEN_POWER_JOB_PROTOCOL";
	public static final String FAILED_JOB_PROTOCOL = "FAILED_JOB_PROTOCOL";
	public static final String FAILED_TRANSFER_PROTOCOL = "FAILED_TRANSFER_PROTOCOL";
	public static final String FAILED_SOURCE_TRANSFER_PROTOCOL = "FAILED_SOURCE_TRANSFER_PROTOCOL";


	/**
	 * Power shortage protocols
	 * POWER_SHORTAGE_ALERT_PROTOCOL                - protocol used in messages informing the agent that the power shortage was detected
	 * POWER_SHORTAGE_FINISH_ALERT_PROTOCOL         - protocol used in messages informing that the power shortage for given agent has finished
	 * SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL       - protocol used in messages informing that the job should be put on hold in green source due to
	 * server power shortage
	 * SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL     - protocol used in messages informing the server want to verify if given job can be supplied again using
	 * green energy
	 * POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL     - protocol used in messages confirming that new green source will supplied affected job with green power
	 * SERVER_POWER_SHORTAGE_ALERT_PROTOCOL         - protocol used in informing that the server is affected by the power shortage
	 * POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL       - protocol informing that the job is being transferred on servers' level (between servers from given CNA, not
	 * between green sources from given Server)
	 */
	public static final String POWER_SHORTAGE_ALERT_PROTOCOL = "POWER_SHORTAGE_ALERT_PROTOCOL";
	public static final String POWER_SHORTAGE_FINISH_ALERT_PROTOCOL = "POWER_SHORTAGE_FINISH_ALERT_PROTOCOL";
	public static final String SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL = "SERVER_POWER_SHORTAGE_ON_HOLD";
	public static final String SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL = "SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL";
	public static final String POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL = "POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL";
	public static final String SERVER_POWER_SHORTAGE_ALERT_PROTOCOL = "SERVER_POWER_SHORTAGE_ALERT_PROTOCOL";
	public static final String POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL = "POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL";

	/**
	 * Dynamic com.greencloud.application.weather handling related protocols
	 *
	 * ON_HOLD_JOB_CHECK_PROTOCOL           - protocol used in messages that check the com.greencloud.application.weather before putting the job which was on hold back
	 * to in progress
	 * PERIODIC_WEATHER_CHECK_PROTOCOL      - protocol used in messages that check periodically the current com.greencloud.application.weather
	 */
	public static final String ON_HOLD_JOB_CHECK_PROTOCOL = "ON_HOLD_JOB_CHECK_PROTOCOL";
	public static final String PERIODIC_WEATHER_CHECK_PROTOCOL = "PERIODIC_WEATHER_CHECK_PROTOCOL";
}
