package org.greencloud.commons.enums.job;

import static java.util.stream.Stream.concat;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum describing what is the current status of the job in the network
 */
public enum JobExecutionStatusEnum {

	CREATED,
	PROCESSING,
	ACCEPTED_BY_SERVER,
	ACCEPTED,
	IN_PROGRESS,
	IN_PROGRESS_CLOUD,
	IN_PROGRESS_BACKUP_ENERGY_PLANNED,
	IN_PROGRESS_BACKUP_ENERGY,
	ON_HOLD_TRANSFER,
	ON_HOLD_TRANSFER_PLANNED,
	ON_HOLD_SOURCE_SHORTAGE_PLANNED,
	ON_HOLD_SOURCE_SHORTAGE,
	ON_HOLD_PLANNED,
	ON_HOLD;

	public static final Set<JobExecutionStatusEnum> ACCEPTED_JOB_STATUSES = EnumSet.of(
			ACCEPTED, IN_PROGRESS, IN_PROGRESS_CLOUD,
			IN_PROGRESS_BACKUP_ENERGY_PLANNED, IN_PROGRESS_BACKUP_ENERGY,
			ON_HOLD_PLANNED, ON_HOLD,
			ON_HOLD_TRANSFER_PLANNED, ON_HOLD_TRANSFER,
			ON_HOLD_SOURCE_SHORTAGE_PLANNED, ON_HOLD_SOURCE_SHORTAGE
	);

	public static final Set<JobExecutionStatusEnum> ACCEPTED_BY_SERVER_JOB_STATUSES =
			concat(Stream.of(ACCEPTED_BY_SERVER), ACCEPTED_JOB_STATUSES.stream()).collect(Collectors.toSet());
	public static final Set<JobExecutionStatusEnum> RUNNING_JOB_STATUSES = EnumSet.of(
			IN_PROGRESS, IN_PROGRESS_CLOUD, IN_PROGRESS_BACKUP_ENERGY,
			ON_HOLD, ON_HOLD_TRANSFER, ON_HOLD_SOURCE_SHORTAGE
	);
	public static final Set<JobExecutionStatusEnum> PLANNED_JOB_STATUSES = EnumSet.of(
			ACCEPTED, IN_PROGRESS_BACKUP_ENERGY_PLANNED,
			ON_HOLD_PLANNED, ON_HOLD_SOURCE_SHORTAGE_PLANNED, ON_HOLD_TRANSFER_PLANNED
	);
	public static final Set<JobExecutionStatusEnum> ACTIVE_JOB_STATUSES = EnumSet.of(
			ACCEPTED, IN_PROGRESS, IN_PROGRESS_CLOUD,
			IN_PROGRESS_BACKUP_ENERGY_PLANNED, IN_PROGRESS_BACKUP_ENERGY
	);

	public static final Set<JobExecutionStatusEnum> IN_PROGRESS_JOB_STATUSES = EnumSet.of(
			IN_PROGRESS, IN_PROGRESS_CLOUD, IN_PROGRESS_BACKUP_ENERGY_PLANNED, IN_PROGRESS_BACKUP_ENERGY
	);
	public static final Set<JobExecutionStatusEnum> JOB_ON_HOLD_STATUSES =
			EnumSet.of(ON_HOLD, ON_HOLD_TRANSFER, ON_HOLD_TRANSFER_PLANNED, ON_HOLD_SOURCE_SHORTAGE);
	public static final Set<JobExecutionStatusEnum> POWER_SHORTAGE_SOURCE_STATUSES =
			EnumSet.of(IN_PROGRESS_BACKUP_ENERGY, ON_HOLD_SOURCE_SHORTAGE);
}
