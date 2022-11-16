package com.greencloud.application.messages.domain.constants;

/**
 * Class store conversation identifiers
 */
public class MessageConversationConstants {

	/**
	 * SCHEDULED_JOB_ID		   	   - conversation id used in messages informing that the job has been scheduled for execution
	 * RE_SCHEDULED_JOB_ID		   - conversation id used in messages informing that the job time frames has been adjusted
	 * PROCESSING_JOB_ID		   - conversation id used in messages informing that the job execution is being processed
	 * CONFIRMED_JOB_ID			   - conversation id used in messages informing that the job execution has been confirmed
	 * CONFIRMED_JOB_TRANSFER_ID   - conversation id used in messages informing that the job transfer has been confirmed
	 * STARTED_JOB_ID			   - conversation id used in messages informing that the job execution has started
	 * DELAYED_JOB_ID			   - conversation id used in messages informing that the job execution is being delayed
	 * POSTPONED_JOB_ID			   - conversation id used in messages informing that the job execution is being postponed
	 * BACK_UP_POWER_JOB_ID  	   - conversation id used in messages informing that the job is executed using the back-up power
	 * GREEN_POWER_JOB_ID    	   - conversation id used in messages informing that the job is executed using green power
	 * ON_HOLD_JOB_ID			   - conversation id used in messages informing that the job has been put on hold
	 * FINISH_JOB_ID			   - conversation id used in messages informing that the job execution has finished
	 * FAILED_JOB_ID			   - conversation id used in messages informing that the job execution has failed
	 */
	public static final String SCHEDULED_JOB_ID = "SCHEDULED_JOB_ID";
	public static final String RE_SCHEDULED_JOB_ID = "RE_SCHEDULED_JOB_ID";
	public static final String PROCESSING_JOB_ID = "PROCESSING_JOB_ID";
	public static final String CONFIRMED_JOB_ID = "CONFIRMED_JOB_ID";
	public static final String CONFIRMED_JOB_TRANSFER_ID = "CONFIRMED_JOB_TRANSFER_ID";
	public static final String STARTED_JOB_ID = "STARTED_JOB_ID";
	public static final String DELAYED_JOB_ID = "DELAYED_JOB_ID";
	public static final String POSTPONED_JOB_ID = "POSTPONED_JOB_ID";
	public static final String BACK_UP_POWER_JOB_ID = "BACK_UP_POWER_JOB_ID";
	public static final String GREEN_POWER_JOB_ID = "GREEN_POWER_JOB_ID";
	public static final String ON_HOLD_JOB_ID = "ON_HOLD_JOB_ID";
	public static final String FINISH_JOB_ID = "FINISH_JOB_ID";
	public static final String FAILED_JOB_ID = "FAILED_JOB_ID";
}
