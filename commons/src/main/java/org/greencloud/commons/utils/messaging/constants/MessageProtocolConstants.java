package org.greencloud.commons.utils.messaging.constants;

/**
 * Class stores constants describing the message protocols
 */
public class MessageProtocolConstants {

	/**
	 * Call for proposals protocols
	 *
	 * <p> CMA_JOB_CFP_PROTOCOL        - protocol used in central manager agent's call for proposal message that is
	 * sent to regional managers while looking for the network for job execution </p>
	 * <p> RMA_JOB_CFP_PROTOCOL              - protocol used in regional manager's call for proposal message that is sent
	 * to the servers while looking for the server which will execute the job </p>
	 * <p> SERVER_JOB_CFP_PROTOCOL           - protocol used in the server's call for proposal message that is sent to the green
	 * sources while looking for the green source which will supply the
	 * server with power needed for the job execution </p>
	 */
	public static final String CMA_JOB_CFP_PROTOCOL = "CMA_JOB_CFP";
	public static final String RMA_JOB_CFP_PROTOCOL = "RMA_JOB_CFP";
	public static final String SERVER_JOB_CFP_PROTOCOL = "SERVER_JOB_CFP";

	/**
	 * Job allocation protocols
	 * <p>CMA_JOB_ALLOCATION_PROTOCOl - protocol used when CMA sends a REQUEST to RMA to execute jobs</p>
	 */
	public static final String CMA_JOB_ALLOCATION_PROTOCOl = "CMA_JOB_ALLOCATION_PROTOCOL";
	public static final String RMA_JOB_ALLOCATION_PROTOCOl = "RMA_JOB_ALLOCATION_PROTOCOl";
	public static final String ALLOCATION_DATA_REQUEST = "ALLOCATION_DATA_REQUEST";

	/**
	 * Job state protocols
	 *
	 * <p> ANNOUNCED_JOB_PROTOCOL		   - protocol used in messages informing that the job has been announced in network </p>
	 * <p> MANUAL_JOB_FINISH_PROTOCOL      - protocol used in messages informing that the job had to be finished manually because the time </p>
	 * after which the information about job being finished has passed </p>
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
	public static final String FAILED_JOB_PROTOCOL = "FAILED_JOB_PROTOCOL";
	public static final String FAILED_TRANSFER_PROTOCOL = "FAILED_TRANSFER_PROTOCOL";
	public static final String FAILED_SOURCE_TRANSFER_PROTOCOL = "FAILED_SOURCE_TRANSFER_PROTOCOL";
	public static final String REQUEST_JOB_EXECUTION_TIME = "REQUEST_JOB_EXECUTION_TIME";

	/**
	 * Agent connections protocols
	 *
	 * <p> CONNECT_GREEN_SOURCE_PROTOCOL 	  - protocol used in messages sent by the Green Source informing Server that
	 * it can be added to its connections </p>
	 * <p> DEACTIVATE_GREEN_SOURCE_PROTOCOL	  - protocol used in messages sent by the Green Source informing Server that
	 * it should deactivate given green source </p>
	 * <p> DISCONNECT_GREEN_SOURCE_PROTOCOL	  - protocol used in messages sent by the Green Source informing Server that
	 * it remove given green source connection </p>
	 * <p> DISABLE_SERVER_PROTOCOL	  		  - protocol used in messages sent by the Server informing RMA that
	 * the given Server is disabled </p>
	 * <p> DISABLE_SERVER_PROTOCOL	  		  - protocol used in messages sent by the Server informing RMA that
	 * <p> REGISTER_SERVER_RESOURCES_PROTOCOL - protocol sends information about resources of given Server to RMA</p>
	 * the given Server is enabled </p>
	 * <p> CHANGE_SERVER_RESOURCES_PROTOCOL - protocol sends information about changes in resources of given Server to RMA</p>
	 * the given Server is enabled </p>
	 */
	public static final String CONNECT_GREEN_SOURCE_PROTOCOL = "CONNECT_GREEN_SOURCE_PROTOCOL";
	public static final String DEACTIVATE_GREEN_SOURCE_PROTOCOL = "DEACTIVATE_GREEN_SOURCE_PROTOCOL";
	public static final String DISCONNECT_GREEN_SOURCE_PROTOCOL = "DISCONNECT_GREEN_SOURCE_PROTOCOL";
	public static final String DISABLE_SERVER_PROTOCOL = "DISABLE_SERVER_PROTOCOL";
	public static final String ENABLE_SERVER_PROTOCOL = "ENABLE_SERVER_PROTOCOL";
	public static final String REGISTER_SERVER_RESOURCES_PROTOCOL = "REGISTER_SERVER_RESOURCES_PROTOCOL";
	public static final String CHANGE_SERVER_RESOURCES_PROTOCOL = "CHANGE_SERVER_RESOURCES_PROTOCOL";

	/**
	 * Error protocols
	 *
	 * <p> NETWORK_ERROR_ALERT_PROTOCOL                	- protocol used in messages informing the agent that the network
	 * error was detected </p>
	 * <p> NETWORK_ERROR_FINISH_ALERT_PROTOCOL          - protocol used in messages informing that the network error
	 * for given agent has finished </p>
	 * <p> INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL       - protocol used in messages informing that the job should
	 * be put on hold in green source due to internal server error </p>
	 * <p> SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL     - protocol used in messages informing the server want to verify
	 * if given job can be supplied again using green energy </p>
	 * <p> POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL     - protocol used in messages confirming that new green source
	 * will supplied affected job with green power </p>
	 * <p> INTERNAL_SERVER_ERROR_ALERT_PROTOCOL         - protocol used in informing that the server is affected by
	 * the internal error causing its failure </p>
	 * <p> POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL       - protocol informing that the job is being transferred on servers'
	 * level (between servers from given RMA, not between
	 * green sources from given Server) </p>
	 */
	public static final String NETWORK_ERROR_ALERT_PROTOCOL = "NETWORK_ERROR_ALERT_PROTOCOL";
	public static final String NETWORK_ERROR_FINISH_ALERT_PROTOCOL = "NETWORK_ERROR_FINISH_ALERT_PROTOCOL";
	public static final String INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL = "INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL";
	public static final String SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL = "SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL";
	public static final String POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL = "POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL";
	public static final String INTERNAL_SERVER_ERROR_ALERT_PROTOCOL = "INTERNAL_SERVER_ERROR_ALERT_PROTOCOL";
	public static final String POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL = "POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL";

	/**
	 * Dynamic weather handling related protocols
	 *
	 * <p> ON_HOLD_JOB_CHECK_PROTOCOL           - protocol used in messages that check the weather before putting
	 * the job which was on hold back to in progress </p>
	 * <p> PERIODIC_WEATHER_CHECK_PROTOCOL      - protocol used in messages that check periodically the current weather </p>
	 */
	public static final String ON_HOLD_JOB_CHECK_PROTOCOL = "ON_HOLD_JOB_CHECK_PROTOCOL";
	public static final String PERIODIC_WEATHER_CHECK_PROTOCOL = "PERIODIC_WEATHER_CHECK_PROTOCOL";

	/**
	 * Rule set adaptation protocols
	 *
	 * <p> CHANGE_RULE_SET_PROTOCOL           - protocol used in messages asking underlying agents to adapt new rule set
	 * <p> REMOVE_RULE_SET_PROTOCOL           - protocol used in messages asking underlying agents to remove rule set
	 */
	public static final String CHANGE_RULE_SET_PROTOCOL = "CHANGE_RULE_SET_PROTOCOL";
	public static final String REMOVE_RULE_SET_PROTOCOL = "REMOVE_RULE_SET_PROTOCOL";

	/**
	 * System adaptation protocols
	 *
	 * <p> CONFIRM_SYSTEM_PLAN_MESSAGE - protocol used in messages that confirm that the execution of
	 * system plan was successful </p>
	 * <p> EXECUTE_ACTION_PROTOCOL - protocol used in messages that initiate system adaptation </p>
	 */
	public static final String CONFIRM_SYSTEM_PLAN_MESSAGE = "CONFIRM_SYSTEM_PLAN_MESSAGE";
	public static final String EXECUTE_ACTION_PROTOCOL = "EXECUTE_ACTION_PROTOCOL";

	/**
	 * Cost-related protocols
	 *
	 * <p> EXECUTION_PRICE_MESSAGE - protocol used in messages that pass the information about job execution price </p>
	 * <p> ESTIMATED_PRICE_MESSAGE - protocol used in messages that pass the information about estimated job price </p>
	 * <p> FINAL_EXECUTION_PRICE_MESSAGE - protocol used in messages that pass the information about final job execution price </p>
	 */
	public static final String EXECUTION_PRICE_MESSAGE = "EXECUTION_PRICE_MESSAGE";
	public static final String ESTIMATED_PRICE_MESSAGE = "ESTIMATED_PRICE_MESSAGE";
	public static final String FINAL_EXECUTION_PRICE_MESSAGE = "FINAL_EXECUTION_PRICE_MESSAGE";

}
