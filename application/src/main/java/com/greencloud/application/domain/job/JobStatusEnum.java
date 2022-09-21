package com.greencloud.application.domain.job;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum describing what is the current status of the job in the network
 */
public enum JobStatusEnum {
	PROCESSING,
	ACCEPTED,
	IN_PROGRESS,
	IN_PROGRESS_BACKUP_ENERGY,
	ON_HOLD_TRANSFER,
	ON_HOLD_SOURCE_SHORTAGE,
	ON_HOLD;

	public static Set<JobStatusEnum> ACCEPTED_JOB_STATUSES = EnumSet.of(IN_PROGRESS, IN_PROGRESS_BACKUP_ENERGY, ON_HOLD,
			ON_HOLD_TRANSFER, ON_HOLD_SOURCE_SHORTAGE, ACCEPTED);
	public static Set<JobStatusEnum> RUNNING_JOB_STATUSES = EnumSet.of(IN_PROGRESS, IN_PROGRESS_BACKUP_ENERGY, ON_HOLD,
			ON_HOLD_TRANSFER, ON_HOLD_SOURCE_SHORTAGE);
	public static Set<JobStatusEnum> ACTIVE_JOB_STATUSES = EnumSet.of(IN_PROGRESS, IN_PROGRESS_BACKUP_ENERGY, ACCEPTED);
	public static Set<JobStatusEnum> JOB_ON_HOLD = EnumSet.of(ON_HOLD, ON_HOLD_TRANSFER, ON_HOLD_SOURCE_SHORTAGE);
}
