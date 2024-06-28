package org.greencloud.commons.utils.job;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.greencloud.commons.constants.MonitoringConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.RUNNING_JOB_STATUSES;
import static org.greencloud.commons.mapper.JobMapper.mapToJobDurationAndStartAndInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobStatusWithTime;
import org.greencloud.commons.domain.job.extended.JobStatusWithTime;
import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.jetbrains.annotations.Nullable;

import jade.core.AID;

/**
 * Class defines set of utilities used to handle jobs
 */
public class JobUtils {

	public static final Long MAX_ERROR_IN_JOB_FINISH = 1000L;

	/**
	 * Method retrieves the job by the job id from job map
	 *
	 * @param jobId  job identifier
	 * @param jobMap map to traverse
	 * @return job
	 */
	@Nullable
	public static <T extends PowerJob> T getJobById(final String jobId, final Map<T, JobExecutionStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the job by the job id from job map
	 *
	 * @param jobInstanceId job instance identifier
	 * @param jobMap        map to traverse
	 * @return job
	 */
	@Nullable
	public static <T extends PowerJob> T getJobByInstanceId(final String jobInstanceId,
			final Map<T, JobExecutionStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobInstanceId().equals(jobInstanceId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the server job by the job id, start time and corresponding server from job map
	 *
	 * @param jobInstanceId unique identifier of the job instance
	 * @param server        server of interest
	 * @param jobMap        map to traverse
	 * @return ServerJob
	 */
	@Nullable
	public static ServerJob getJobByInstanceIdAndServer(final String jobInstanceId, final AID server,
			final Map<ServerJob, JobExecutionStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobInstanceId().equals(jobInstanceId) && job.getServer().equals(server))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method returns the instance of the job for current time
	 *
	 * @param jobId  unique job identifier
	 * @param jobMap map to traverse
	 * @return pair of job and current status
	 */
	@Nullable
	public static <T extends PowerJob> Map.Entry<T, JobExecutionStatusEnum> getCurrentJobInstance(final String jobId,
			final Map<T, JobExecutionStatusEnum> jobMap) {
		final Map<T, JobExecutionStatusEnum> jobsMatchingId = jobMap.entrySet().stream()
				.filter(job -> job.getKey().getJobId().equals(jobId))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		if (jobsMatchingId.entrySet().stream().allMatch(job -> isNull(job.getKey().getStartTime()))) {
			return jobsMatchingId.entrySet().stream().findFirst().orElse(null);
		}

		return jobMap.entrySet().stream()
				.filter(jobEntry -> nonNull(jobEntry.getKey().getStartTime()))
				.max(comparing(jobEntry -> jobEntry.getKey().getStartTime()))
				.orElse(null);
	}

	/**
	 * Method returns number of jobs which execution status is contained in the given set
	 *
	 * @param jobMap   map of jobs to count
	 * @param statuses set of job statuses of interest
	 * @return integer being the number of jobs on hold
	 */
	public static <T extends PowerJob> int getJobCount(final Map<T, JobExecutionStatusEnum> jobMap,
			final Set<JobExecutionStatusEnum> statuses) {
		return jobMap.entrySet().stream()
				.filter(job -> statuses.contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(PowerJob::getJobId)
				.collect(toSet())
				.size();
	}

	/**
	 * Method returns number of currently started jobs
	 *
	 * @return integer being the number of currently started jobs
	 */
	public static <T extends PowerJob> int getJobCount(final Map<T, JobExecutionStatusEnum> jobMap) {
		return jobMap.entrySet().stream()
				.filter(job -> isJobStarted(job.getValue()))
				.map(Map.Entry::getKey)
				.map(PowerJob::getJobId)
				.collect(toSet())
				.size();
	}

	/**
	 * Method verifies if the given job has started
	 *
	 * @param job    job of interest
	 * @param jobMap map to traverse
	 * @return boolean indicating if a given job has started
	 */
	public static <T extends PowerJob> boolean isJobStarted(final T job, final Map<T, JobExecutionStatusEnum> jobMap) {
		return RUNNING_JOB_STATUSES.contains(jobMap.get(job));
	}

	/**
	 * Method verifies if the given job has started
	 *
	 * @param jobStatus current job status
	 * @return boolean indicating if a given job has started
	 */
	public static boolean isJobStarted(final JobExecutionStatusEnum jobStatus) {
		return RUNNING_JOB_STATUSES.contains(jobStatus);
	}

	/**
	 * Method verifies if there is only 1 instance of the given job
	 *
	 * @param jobId  unique job identifier
	 * @param jobMap map to traverse
	 * @return boolean
	 */
	public static <T extends PowerJob> boolean isJobUnique(final String jobId,
			final Map<T, JobExecutionStatusEnum> jobMap) {
		return jobMap.keySet().stream().filter(job -> job.getJobId().equals(jobId)).toList().size() == 1;
	}

	/**
	 * Function computes ratio of succeeded jobs based on number of accepted and failed jobs
	 *
	 * @param acceptedJobs number of accepted jobs
	 * @param failedJobs   number of failed jobs
	 * @return double job success ratio or -1 if data is not available
	 */
	public static double getJobSuccessRatio(final long acceptedJobs, final long failedJobs) {
		return acceptedJobs == 0 ? DATA_NOT_AVAILABLE_INDICATOR : 1 - ((double) failedJobs / acceptedJobs);
	}

	/**
	 * Method retrieves list of time instances based on jobs' time-frames.
	 * It includes also job that is not in the processed job map.
	 *
	 * @param additionalJob - job that is to be included apart from jobs in the map
	 * @param jobMap        map of the jobs
	 * @return list of time instances
	 */
	public static <T extends PowerJob> List<Instant> getTimetableOfJobs(final T additionalJob,
			final Map<T, JobExecutionStatusEnum> jobMap) {
		var validJobs = jobMap.entrySet().stream()
				.filter(entry -> ACCEPTED_JOB_STATUSES.contains(entry.getValue()))
				.map(Map.Entry::getKey)
				.toList();

		return Stream.concat(
						Stream.of(
								convertToRealTime(additionalJob.getStartTime()),
								convertToRealTime(additionalJob.getExpectedEndTime())),
						Stream.concat(
								validJobs.stream().map(job -> convertToRealTime(job.getStartTime())),
								validJobs.stream().map(job -> convertToRealTime(job.getExpectedEndTime()))))
				.distinct()
				.toList();
	}

	/**
	 * Method calculates expected job end time taking into account possible time error
	 *
	 * @param job job of interest
	 * @return date of expected job end time
	 */
	public static Date calculateExpectedJobEndTime(final PowerJob job) {
		final Instant endDate = alignStartTimeToCurrentTime(job.getExpectedEndTime());
		return Date.from(endDate.plus(MAX_ERROR_IN_JOB_FINISH, ChronoUnit.MILLIS));
	}

	/**
	 * Method updates job execution time
	 *
	 * @param job           job which time is to be updated
	 * @param executionTime new execution time estimation
	 * @param startTime     new job start time
	 * @param jobMap        map with all jobs
	 */
	public static <T extends PowerJob> T updateJobStartAndExecutionTime(final T job, final Instant startTime,
			final long executionTime, final Map<T, JobExecutionStatusEnum> jobMap) {
		final T jobWithNewExecutionTime = mapToJobDurationAndStartAndInstanceId(job, startTime, executionTime);
		final JobExecutionStatusEnum currentStatus = jobMap.remove(job);

		ofNullable(currentStatus).ifPresent(status -> jobMap.put(jobWithNewExecutionTime, currentStatus));
		return jobWithNewExecutionTime;
	}

	/**
	 * Method initializes empty map of timers and job statuses
	 *
	 * @return map with initialized timers for statuses
	 */
	public static ConcurrentMap<JobExecutionStatusEnum, JobStatusWithTime> initializeJobStatusDurationMap() {
		return new ConcurrentHashMap<>(stream(JobExecutionStatusEnum.values()).collect(
				toMap(status -> status,
						status -> ImmutableJobStatusWithTime.builder()
								.duration(new AtomicLong(0))
								.timer(new Timer())
								.build())));
	}

	/**
	 * Method returns message conversationId based on job status
	 *
	 * @param currentStatus job status
	 * @return conversationId
	 */
	public static String getMessageConversationId(final JobExecutionStatusEnum currentStatus) {
		return switch (currentStatus) {
			case ACCEPTED -> GREEN_POWER_JOB_ID;
			case ON_HOLD_SOURCE_SHORTAGE_PLANNED, ON_HOLD_PLANNED, ON_HOLD_TRANSFER_PLANNED -> ON_HOLD_JOB_ID;
			case IN_PROGRESS_BACKUP_ENERGY_PLANNED -> BACK_UP_POWER_JOB_ID;
			default -> null;
		};
	}

}
