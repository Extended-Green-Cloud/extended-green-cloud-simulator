package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler.logs;

/**
 * Class contains all constants used in logging information in handler behaviours during job scheduling process
 */
public class JobSchedulingHandlerLog {

	// JOB ANNOUNCEMENT LOG MESSAGES
	public static final String NO_AVAILABLE_CNA_LOG = "There are no available Cloud Network Agents!";
	public static final String ANNOUNCE_JOB_CNA_LOG = "Looking for Cloud Network for job {} execution";
	public static final String JOB_EXECUTION_AFTER_DEADLINE_LOG = "Sending FAIL information to Client. Job {} would be executed after deadline";
	public static final String JOB_ADJUST_TIME_LOG = "Job {} time frames are outdated. Adjusting job time frames to current time.";

}
