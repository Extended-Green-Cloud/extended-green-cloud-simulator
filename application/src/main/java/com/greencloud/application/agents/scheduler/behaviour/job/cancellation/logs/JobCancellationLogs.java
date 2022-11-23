package com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs;

public final class JobCancellationLogs {

	public static final String SUCCESSFUL_JOB_CANCELLATION_LOG =
			"Successfully finished processing after failed part of a job! All parts finished processing,"
			+ " including {} cancellations.";
	public static final String NOT_ALL_JOB_PARTS_CANCELLED_LOG =
			"Not all job parts were cancelled! {} still remaining.";

	public static final String CANCELLING_JOB_PARTS_LOG = "Cancelling {} job parts!";
	public static final String CANCELLED_JOB_PART_LOG = "Cancelled job part!";

	private JobCancellationLogs() {
	}
}
