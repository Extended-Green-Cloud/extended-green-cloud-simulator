package com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs;

/**
 * Class contains all constants used in logging information in handler behaviours during job announcement process
 */
public class JobAnnouncementHandlerLog {

	// HANDLE JOB FAILURE LOG MESSAGES
	public static final String PART_FAILED_LOG = "One of my job parts failed! Agent shutdown initiated.";

	// HANDLE JOB FINISH LOG MESSAGE
	public static final String CLIENT_JOB_FINISHED_LOG = "Job finished! Agent shutdown initiated!";
	public static final String ALL_PARTS_FINISHED_LOG = "All job parts finished! Agent shutdown initiated.";
	public static final String CLIENT_JOB_FINISH_ON_TIME_LOG = "The execution of my job {} finished on time! :)";
	public static final String CLIENT_JOB_FINISH_DELAY_LOG =
			"The execution of my job {} finished with a delay equal to {}! :(";
	public static final String CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_DELAY_LOG =
			"The execution of my job {} finished (with delay {} min) before deadline";
	public static final String CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_LOG = "The execution of my job {} finished!";

	// HANDLE JOB START LOG MESSAGES
	public static final String ALL_PARTS_STARTED_LOG = "All job parts started!";
	public static final String CLIENT_JOB_START_ON_TIME_LOG = "The execution of my job started on time! :)";
	public static final String CLIENT_JOB_START_DELAY_LOG =
			"The execution of my job started {} min after the preferred start time";

	// HANDLE JOB SPLIT LOG MESSAGES
	public static final String CLIENT_JOB_SPLIT_LOG = "My job has been split.";
}
