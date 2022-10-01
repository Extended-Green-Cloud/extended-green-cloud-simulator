package com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours for power shortage
 */
public class PowerShortageServerInitiatorLog {

	// POWER SHORTAGE GREEN SOURCE TRANSFER LOG MESSAGES
	public static final String GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG = "No responses were retrieved for job transfer";
	public static final String GS_TRANSFER_NONE_AVAILABLE_LOG =
			"No green sources are available for the power transfer of job {}. "
					+ "Passing the information to the Cloud Network";
	public static final String GS_TRANSFER_CHOSEN_GS_LOG = "Chosen Green Source for the job {} transfer: {}";
	public static final String GS_TRANSFER_FAIL_NO_BACK_UP_LOG =
			"There is not enough back up power to support the job {}. Putting job on hold";
	public static final String GS_TRANSFER_FAIL_BACK_UP_LOG = "Putting the job {} on back up power";

	// POWER SHORTAGE CNA TRANSFER LOG MESSAGES
	public static final String CNA_JOB_TRANSFER_PROCESSING_LOG = "Cloud Network {} is working on the job {} transfer";
	public static final String CNA_JOB_TRANSFER_REFUSE_LOG =
			"Cloud Network {} refused to work on job {} transfer. The job was not found.";
	public static final String CNA_JOB_TRANSFER_SUCCESSFUL_LOG =
			"Transfer of job with id {} was established successfully. Finishing the job and informing the green source";
	public static final String CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG =
			"Transfer of job with id {} was unsuccessful! Putting job on hold";
	public static final String CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG =
			"Transfer of job with id {} was unsuccessful! There is not enough back up power to support the job. "
					+ "Putting job on hold";
	public static final String CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG =
			"Transfer of job with id {} was unsuccessful! Putting the job on back up power";

}
