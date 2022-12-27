package com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours for power shortage in green source
 */
public class PowerShortageSourceInitiatorLog {

	// POWER SHORTAGE IN GREEN SOURCE TRANSFER REQUEST LOG MESSAGES
	public static final String SOURCE_JOB_TRANSFER_PROCESSING_LOG = "Server {} is working on the job {} transfer.";
	public static final String SOURCE_JOB_TRANSFER_REFUSE_NOT_FOUND_LOG =
			"The job {} does not exist anymore. Finishing the job.";
	public static final String SOURCE_JOB_TRANSFER_REFUSE_LOG =
			"Server refused to transfer the job instance {}.";
	public static final String SOURCE_JOB_TRANSFER_SUCCESSFUL_LOG =
			"Transfer of job with id {} was established successfully. Finishing job on power shortage.";
	public static final String SOURCE_JOB_TRANSFER_SUCCESSFUL_NOT_FOUND_LOG =
			"The job with id {} has finished before transfer.";
	public static final String SOURCE_JOB_TRANSFER_FAILURE_LOG =
			"Transfer of job with id {} was unsuccessful! Putting the job on hold.";
	public static final String SOURCE_JOB_TRANSFER_FAILURE_NOT_FOUND_LOG =
			"The job with id {} has finished before putting it on hold.";
}
