package com.greencloud.application.agents.scheduler.domain;

import java.time.temporal.ValueRange;

/**
 * Class storing Scheduler Agent constants:
 * <p> QUEUE_CAPACITY_THRESHOLD 	- threshold of the jobs queue size </p>
 * <p> SEND_NEXT_JOB_TIMEOUT		- timeout in between consecutive job announcements</p>
 * <p> MAX_TRAFFIC_DIFFERENCE 		- value range describing the maximum difference in power in use for network segment that can be
 * 									  neglected in Cloud Network selection </p>
 * <p> JOB_RETRY_MINUTES_ADJUSTMENT - time in minutes (of real time) to which job start and end should be postponed</p>
 */
public class SchedulerAgentConstants {

	public static final int QUEUE_CAPACITY_THRESHOLD = 10000000;
	public static final int SEND_NEXT_JOB_TIMEOUT = 500;
	public static final ValueRange MAX_TRAFFIC_DIFFERENCE = ValueRange.of(-2, 2);
	public static final Integer JOB_RETRY_MINUTES_ADJUSTMENT = 60;
}
