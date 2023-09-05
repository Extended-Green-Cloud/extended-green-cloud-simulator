package com.greencloud.application.agents.scheduler.constants;

/**
 * Class storing Scheduler Agent constants:
 * <p> SEND_NEXT_JOB_TIMEOUT		- timeout in between consecutive job announcements</p>
 * <p> JOB_RETRY_MINUTES_ADJUSTMENT - time in minutes (of real time) to which job start and end should be postponed</p>
 * <p> JOB_PROCESSING_TIME_ADJUSTMENT - time that may take to process the job </p>
 * <p> JOB_PROCESSING_DEADLINE_ADJUSTMENT - time gap before job deadline </p>
 */
public class SchedulerAgentConstants {

	public static final int SEND_NEXT_JOB_TIMEOUT = 300;
	public static final Integer JOB_RETRY_MINUTES_ADJUSTMENT = 10;
	public static final int JOB_PROCESSING_TIME_ADJUSTMENT = 3000;
	public static final int JOB_PROCESSING_DEADLINE_ADJUSTMENT = 1000;
	public static final int SCHEDULER_MESSAGE_BATCH = 20;
}
