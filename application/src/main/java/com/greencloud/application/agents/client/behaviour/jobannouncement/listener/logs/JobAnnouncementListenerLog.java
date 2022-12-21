package com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs;

/**
 * Class contains all constants used in logging information in listener behaviours during job announcement process
 */
public class JobAnnouncementListenerLog {

	// JOB UPDATE LOG MESSAGES
	public static final String CLIENT_JOB_SCHEDULED_LOG = "My job has been scheduled for execution!";
	public static final String CLIENT_JOB_PROCESSED_LOG = "The execution of my job is being processed!";
	public static final String CLIENT_JOB_DELAY_LOG = "The execution of my job has some delay! :(";
	public static final String CLIENT_JOB_POSTPONE_LOG = "The execution of my job is being postponed.";
	public static final String CLIENT_JOB_BACK_UP_LOG = "My job is being executed using the back up power!";
	public static final String CLIENT_JOB_GREEN_POWER_LOG = "My job is again being executed using the green power!";
	public static final String CLIENT_JOB_ON_HOLD_LOG = "My job has been put on hold :(!";
	public static final String CLIENT_JOB_START_ON_TIME_LOG = "The execution of my job started on time! :)";
	public static final String CLIENT_JOB_START_DELAY_LOG = "The execution of my job started {} min after the preferred start time";
	public static final String CLIENT_JOB_FINISH_ON_TIME_LOG = "The execution of my job finished on time! :)";
	public static final String CLIENT_JOB_FINISH_DELAY_LOG = "The execution of my job finished with a delay equal to {}! :(";
	public static final String CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_DELAY_LOG =
			"The execution of my job finished (with delay {} min) before deadline";
	public static final String CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_LOG = "The execution of my job finished!";
	public static final String CLIENT_JOB_FAILED_LOG = "The execution of my job has failed :(";
	public static final String CLIENT_JOB_SPLIT_LOG = "My job has been split.";
	public static final String CLIENT_JOB_RESCHEDULED_LOG = "The time frames of my job has been adjusted.";
	public static final String CLIENT_JOB_FINISHED_LOG = "Job finished! Agent shutdown initiated!";

	// SPLIT JOB LOGS
	public static final String ALL_PARTS_STARTED = "All job parts started!";
	public static final String ALL_PARTS_FINISHED = "All job parts finished! Agent shutdown initiated.";
	public static final String PART_FAILED = "One of my job parts failed! Agent shutdown initiated.";
}
