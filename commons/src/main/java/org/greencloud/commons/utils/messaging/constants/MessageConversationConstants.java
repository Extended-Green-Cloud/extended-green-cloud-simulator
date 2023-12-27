package org.greencloud.commons.utils.messaging.constants;

/**
 * Class store conversation identifiers
 */
public class MessageConversationConstants {

	/**
	 * Conversation id used in messages informing that:
	 *
	 * <p> SCHEDULED_JOB_ID		   	   - the job has been scheduled for execution </p>
	 * <p> RE_SCHEDULED_JOB_ID		   - the job time frames has been adjusted </p>
	 * <p> PROCESSING_JOB_ID		   - the job execution is being processed </p>
	 * <p> CONFIRMED_JOB_ID			   - the job execution has been confirmed </p>
	 * <p> STARTED_JOB_ID			   - the job execution has started </p>
	 * <p> STARTED_IN_CLOUD_JOB_ID	   - the job execution has started in cloud </p>
	 * <p> DELAYED_JOB_ID			   - the job execution is being delayed </p>
	 * <p> POSTPONED_JOB_ID			   - the job execution is being postponed </p>
	 * <p> BACK_UP_POWER_JOB_ID  	   - the job is executed using the back-up power </p>
	 * <p> GREEN_POWER_JOB_ID    	   - the job is executed using green power </p>
	 * <p> ON_HOLD_JOB_ID			   - the job has been put on hold </p>
	 * <p> FINISH_JOB_ID			   - the job execution has finished </p>
	 * <p> FINISH_IN_CLOUD_JOB_ID	   - the job execution has finished in cloud </p>
	 * <p> FAILED_JOB_ID			   - the job execution has failed </p>
	 * <p> STARTED_IN_CLOUD_ID		   - the job is being executed by central cloud </p>
	 * <p> UPDATE_JOB_EXECUTOR_ID      - the job executor has changed </p>
	 */
	public static final String SCHEDULED_JOB_ID = "SCHEDULED_JOB_ID";
	public static final String RE_SCHEDULED_JOB_ID = "RE_SCHEDULED_JOB_ID";
	public static final String PROCESSING_JOB_ID = "PROCESSING_JOB_ID";
	public static final String CONFIRMED_JOB_ID = "CONFIRMED_JOB_ID";
	public static final String ACCEPTED_JOB_ID = "ACCEPTED_JOB_ID";
	public static final String STARTED_JOB_ID = "STARTED_JOB_ID";
	public static final String STARTED_IN_CLOUD_JOB_ID = "STARTED_IN_CLOUD_JOB_ID";
	public static final String DELAYED_JOB_ID = "DELAYED_JOB_ID";
	public static final String POSTPONED_JOB_ID = "POSTPONED_JOB_ID";
	public static final String BACK_UP_POWER_JOB_ID = "BACK_UP_POWER_JOB_ID";
	public static final String GREEN_POWER_JOB_ID = "GREEN_POWER_JOB_ID";
	public static final String ON_HOLD_JOB_ID = "ON_HOLD_JOB_ID";
	public static final String FINISH_JOB_ID = "FINISH_JOB_ID";
	public static final String FINISH_IN_CLOUD_JOB_ID = "FINISH_IN_CLOUD_JOB_ID";
	public static final String FAILED_JOB_ID = "FAILED_JOB_ID";
	public static final String STARTED_IN_CLOUD_ID = "STARTED_IN_CLOUD_ID";
	public static final String UPDATE_JOB_EXECUTOR_ID = "UPDATE_JOB_EXECUTOR_ID";
}
