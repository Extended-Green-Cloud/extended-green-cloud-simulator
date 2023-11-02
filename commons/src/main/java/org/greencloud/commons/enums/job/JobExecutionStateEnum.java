package org.greencloud.commons.enums.job;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.greencloud.commons.domain.job.basic.PowerJob;

/**
 * Enum describing what is the current state of the job execution in the network.
 * This enum differs from the {@link JobExecutionStatusEnum} by the fact that it aggregates the active/planned
 * statuses in corresponding pairs.
 */
public enum JobExecutionStateEnum {

	PRE_EXECUTION(JobExecutionStatusEnum.PROCESSING, JobExecutionStatusEnum.CREATED),
	EXECUTING_ON_GREEN(JobExecutionStatusEnum.IN_PROGRESS, JobExecutionStatusEnum.ACCEPTED),
	EXECUTING_ON_BACK_UP(
			JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY, JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED),
	EXECUTING_ON_HOLD(JobExecutionStatusEnum.ON_HOLD, JobExecutionStatusEnum.ON_HOLD_PLANNED),
	EXECUTING_ON_HOLD_SOURCE(
			JobExecutionStatusEnum.ON_HOLD_SOURCE_SHORTAGE, JobExecutionStatusEnum.ON_HOLD_SOURCE_SHORTAGE_PLANNED),
	EXECUTING_TRANSFER(JobExecutionStatusEnum.ON_HOLD_TRANSFER, JobExecutionStatusEnum.ON_HOLD_TRANSFER_PLANNED);

	private final JobExecutionStatusEnum activeStatus;
	private final JobExecutionStatusEnum plannedStatus;

	JobExecutionStateEnum(final JobExecutionStatusEnum activeStatus, final JobExecutionStatusEnum plannedStatus) {
		this.activeStatus = activeStatus;
		this.plannedStatus = plannedStatus;
	}

	/**
	 * Method replaces changes the status of given job into active
	 *
	 * @param jobsMap map of jobs in which the status is to be updated
	 * @param job     job for which status is to be updated
	 *
	 * @return new status
	 */
	public static <T extends PowerJob> JobExecutionStatusEnum replaceStatusToActive(
			final ConcurrentMap<T, JobExecutionStatusEnum> jobsMap, final T job) {
		final JobExecutionStateEnum currentState = findStateByPlannedStatus(jobsMap.get(job));
		final JobExecutionStatusEnum newStatus = nonNull(currentState) ? currentState.activeStatus : jobsMap.get(job);
		jobsMap.replace(job, newStatus);
		return newStatus;
	}

	/**
	 * Method returns information if given status is active or not and additionally validates if it is included in
	 * the set of considered statuses
	 *
	 * @param status             status to verify
	 * @param consideredStatuses statuses to take into account
	 * @return boolean
	 */
	public static Boolean isStatusActive(final JobExecutionStatusEnum status,
			final JobExecutionStatusEnum... consideredStatuses) {
		if (asList(consideredStatuses).contains(status)) {
			final JobExecutionStateEnum state = findStateByAnyStatus(status);
			if (nonNull(state)) {
				return state.activeStatus.equals(status);
			}
			return false;
		}
		return null;
	}

	/**
	 * Method returns information if given status is active or not and additionally validates if it is included in
	 * the set of considered states
	 *
	 * @param status           status to verify
	 * @param consideredStates states to take into account
	 * @return boolean
	 */
	public static Boolean isStatusActive(final JobExecutionStatusEnum status,
			final JobExecutionStateEnum... consideredStates) {
		final List<JobExecutionStatusEnum> statuses = stream(consideredStates)
				.map(JobExecutionStateEnum::getStatuses)
				.flatMap(Set::stream)
				.toList();

		if (statuses.contains(status)) {
			final JobExecutionStateEnum state = findStateByAnyStatus(status);
			if (nonNull(state)) {
				return state.activeStatus.equals(status);
			}
			return false;
		}
		return null;
	}

	private static JobExecutionStateEnum findStateByPlannedStatus(final JobExecutionStatusEnum status) {
		return stream(JobExecutionStateEnum.values())
				.filter(state -> state.plannedStatus.equals(status))
				.findFirst()
				.orElse(null);
	}

	private static JobExecutionStateEnum findStateByAnyStatus(final JobExecutionStatusEnum status) {
		return stream(JobExecutionStateEnum.values())
				.filter(state -> state.getStatuses().stream().anyMatch(a -> a.equals(status)))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method returns either active or planned status of given state
	 *
	 * @param isActive flag indicating if method should return active or planned status
	 * @return JobExecutionStatusEnum
	 */
	public JobExecutionStatusEnum getStatus(final boolean isActive) {
		return isActive ? activeStatus : plannedStatus;
	}

	/**
	 * Method retrieves both statuses associated with given execution state
	 *
	 * @return list of statuses
	 */
	public Set<JobExecutionStatusEnum> getStatuses() {
		return Set.of(activeStatus, plannedStatus);
	}
}
