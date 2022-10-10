package com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs;

/**
 * Class contains all constants used in logging information in listener behaviours during job announcement process
 */
public class JobAnnouncementListenerLog {

	// JOB UPDATE LOG MESSAGES
	public static final String CLIENT_JOB_DELAY_LOG = "The execution of my job has some delay! :(";
	public static final String CLIENT_JOB_BACK_UP_LOG = "My job is being executed using the back up power!";
	public static final String CLIENT_JOB_GREEN_POWER_LOG = "My job is again being executed using the green power!";
	public static final String CLIENT_JOB_START_ON_TIME_LOG = "The execution of my job started on time! :)";
	public static final String CLIENT_JOB_START_DELAY_LOG = "The execution of my job started with a delay equal to {}! :(";
	public static final String CLIENT_JOB_FINISH_ON_TIME_LOG = "The execution of my job finished on time! :)";
	public static final String CLIENT_JOB_FINISH_DELAY_LOG = "The execution of my job finished with a delay equal to {}! :(";
	public static final String CLIENT_JOB_FAILED_LOG = "The execution of my job has failed! :(";
	public static final String CLIENT_JOB_FAILED_RETRY_LOG = "The execution of my job has failed. Retrying for {} to have the job executed.";
}
