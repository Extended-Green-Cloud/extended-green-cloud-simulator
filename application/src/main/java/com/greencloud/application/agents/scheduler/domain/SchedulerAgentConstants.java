package com.greencloud.application.agents.scheduler.domain;

import java.time.temporal.ValueRange;

/**
 * Class storing Scheduler Agent constants:
 * <p> SEND_NEXT_JOB_TIMEOUT		- timeout in between consecutive job announcements</p>
 * <p> MAX_TRAFFIC_DIFFERENCE 		- value range describing the maximum difference in power in use for network segment that can be
 * 									  neglected in Cloud Network selection </p>
 * <p> JOB_RETRY_MINUTES_ADJUSTMENT - time in minutes (of real time) to which job start and end should be postponed</p>
 * <p> JOB_PROCESSING_TIME_ADJUSTMENT - time that may take to process the job </p>
 * <p> JOB_PROCESSING_DEADLINE_ADJUSTMENT - time gap before job deadline </p>
 */
public class SchedulerAgentConstants {

	public static final int SEND_NEXT_JOB_TIMEOUT = 100;
	public static final ValueRange MAX_TRAFFIC_DIFFERENCE = ValueRange.of(-2, 2);
	public static final Integer JOB_RETRY_MINUTES_ADJUSTMENT = 10;
	public static final int JOB_PROCESSING_TIME_ADJUSTMENT = 200;
	public static final int JOB_PROCESSING_DEADLINE_ADJUSTMENT = 200;
}
