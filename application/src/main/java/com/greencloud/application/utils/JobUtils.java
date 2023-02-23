package com.greencloud.application.utils;

import static com.greencloud.application.common.constant.DataConstant.DATA_NOT_AVAILABLE_INDICATOR;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.RUNNING_JOB_STATUSES;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.commons.job.ServerJob;

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
	public static <T extends PowerJob> T getJobById(final String jobId, final Map<T, ExecutionJobStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the job by the job id and start time from job map
	 *
	 * @param jobId     job identifier
	 * @param startTime job start time
	 * @param jobMap    map to traverse
	 * @return job
	 */
	@Nullable
	public static <T extends PowerJob> T getJobByIdAndStartDate(final String jobId, final Instant startTime,
			final Map<T, ExecutionJobStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobId) && job.getStartTime().equals(startTime))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the job by the job id and start time from job map
	 *
	 * @param jobInstanceId unique identifier of the job instance
	 * @param jobMap        map to traverse
	 * @return job
	 */
	@Nullable
	public static <T extends PowerJob> T getJobByIdAndStartDate(final JobInstanceIdentifier jobInstanceId,
			final Map<T, ExecutionJobStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobInstanceId.getJobId())
						&& job.getStartTime().equals(jobInstanceId.getStartTime()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the job by the job id and end time from job map
	 *
	 * @param jobId   job identifier
	 * @param endTime job end time
	 * @param jobMap  map to traverse
	 * @return job
	 */
	@Nullable
	public static <T extends PowerJob> T getJobByIdAndEndDate(final String jobId, final Instant endTime,
			final Map<T, ExecutionJobStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobId) && job.getEndTime().equals(endTime))
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
	public static ServerJob getJobByIdAndStartDateAndServer(final JobInstanceIdentifier jobInstanceId, final AID server,
			final Map<ServerJob, ExecutionJobStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobInstanceId.getJobId())
						&& job.getStartTime().equals(jobInstanceId.getStartTime())
						&& job.getServer().equals(server))
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
	public static <T extends PowerJob> Map.Entry<T, ExecutionJobStatusEnum> getCurrentJobInstance(final String jobId,
			final Map<T, ExecutionJobStatusEnum> jobMap) {
		final Instant currentTime = getCurrentTime();
		return jobMap.entrySet().stream().filter(jobEntry -> {
			final T job = jobEntry.getKey();
			return job.getJobId().equals(jobId) && (
					(job.getStartTime().isBefore(currentTime) && job.getEndTime().isAfter(currentTime))
							|| job.getEndTime().equals(currentTime));
		}).findFirst().orElse(null);
	}

	/**
	 * Method verifies if the given job has started
	 *
	 * @param job    job of interest
	 * @param jobMap map to traverse
	 * @return boolean indicating if a given job has started
	 */
	public static <T extends PowerJob> boolean isJobStarted(final T job, final Map<T, ExecutionJobStatusEnum> jobMap) {
		return RUNNING_JOB_STATUSES.contains(jobMap.get(job));
	}

	/**
	 * Method verifies if the given job has started
	 *
	 * @param jobStatus current job status
	 * @return boolean indicating if a given job has started
	 */
	public static boolean isJobStarted(final ExecutionJobStatusEnum jobStatus) {
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
			final Map<T, ExecutionJobStatusEnum> jobMap) {
		return jobMap.keySet().stream().filter(job -> job.getJobId().equals(jobId)).toList().size() == 1;
	}

	/**
	 * Method calculates expected job end time taking into account possible time error
	 *
	 * @param job job of interest
	 * @return date of expected job end time
	 */
	public static Date calculateExpectedJobEndTime(final PowerJob job) {
		final Instant endDate = getCurrentTime().isAfter(job.getEndTime()) ? getCurrentTime() : job.getEndTime();
		return Date.from(endDate.plus(MAX_ERROR_IN_JOB_FINISH, ChronoUnit.MILLIS));
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
}
