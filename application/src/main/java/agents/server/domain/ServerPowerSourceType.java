package agents.server.domain;

import static domain.job.JobStatusEnum.ACCEPTED;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static domain.job.JobStatusEnum.ON_HOLD;
import static domain.job.JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static domain.job.JobStatusEnum.ON_HOLD_TRANSFER;

import java.util.EnumSet;
import java.util.Set;

import domain.job.JobStatusEnum;

/**
 * Enum describing available power sources for the server
 */
public enum ServerPowerSourceType {
	GREEN_ENERGY(EnumSet.of(IN_PROGRESS, ACCEPTED)),
	BACK_UP_POWER(EnumSet.of(IN_PROGRESS_BACKUP_ENERGY)),
	ALL(EnumSet.of(ACCEPTED, IN_PROGRESS, IN_PROGRESS_BACKUP_ENERGY, ON_HOLD_TRANSFER, ON_HOLD_SOURCE_SHORTAGE,
			ON_HOLD));

	private final Set<JobStatusEnum> jobStatuses;

	ServerPowerSourceType(Set<JobStatusEnum> jobStatuses) {
		this.jobStatuses = jobStatuses;
	}

	public Set<JobStatusEnum> getJobStatuses() {
		return jobStatuses;
	}
}
