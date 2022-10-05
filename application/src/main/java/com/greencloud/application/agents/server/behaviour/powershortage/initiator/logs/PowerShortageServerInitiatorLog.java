package com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs;

/**
 * Class contains all constants used in logging information in initiator behaviours for power shortage
 */
public class PowerShortageServerInitiatorLog {

	// POWER SHORTAGE RE-SUPPLYING JOBS WITH GREEN ENERGY LOG MESSAGES
	public static final String SERVER_RE_SUPPLY_JOB_PROCESSING_LOG = "Green source agreed to process if job {} can be supplied using green energy";
	public static final String SERVER_RE_SUPPLY_NOT_FOUND_LOG = "Job {} was not found in green source";
	public static final String SERVER_RE_SUPPLY_FAILED_LOG = "Green Source failed to re-supply job {} with green power. Cause: {}";
	public static final String SERVER_RE_SUPPLY_REFUSE_NOT_FOUND_SERVER_LOG = "Job {} was not found in server";
	public static final String SERVER_RE_SUPPLY_REFUSE_LOG = "Green source refused to supply job {} again with green power";
	public static final String SERVER_RE_SUPPLY_SUCCESSFUL_LOG = "Green source successfully supplied job {} again with green power. Changing job status in server";

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
