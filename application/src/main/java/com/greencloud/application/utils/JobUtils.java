package com.greencloud.application.utils;

import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.PowerJob;

/**
 * Class defines set of utilities used to handle jobs
 */
public class JobUtils {

	public static final Long MAX_ERROR_IN_JOB_FINISH = 500L;

	/**
	 * Method retrieves the job by the job id from job map
	 *
	 * @param jobId  job identifier
	 * @param jobMap map to traverse
	 * @return job
	 */
	public static <T extends PowerJob> T getJobById(final String jobId, final Map<T, JobStatusEnum> jobMap) {
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
	public static <T extends PowerJob> T getJobByIdAndStartDate(final String jobId, final Instant startTime,
			final Map<T, JobStatusEnum> jobMap) {
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
	public static <T extends PowerJob> T getJobByIdAndStartDate(final JobInstanceIdentifier jobInstanceId,
			final Map<T, JobStatusEnum> jobMap) {
		return jobMap.keySet().stream()
				.filter(job -> job.getJobId().equals(jobInstanceId.getJobId())
						&& job.getStartTime().equals(jobInstanceId.getStartTime()))
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
	public static <T extends PowerJob> Map.Entry<T, JobStatusEnum> getCurrentJobInstance(final String jobId,
			final Map<T, JobStatusEnum> jobMap) {
		final Instant currentTime = getCurrentTime();
		return jobMap.entrySet().stream().filter(jobEntry -> {
			final T job = jobEntry.getKey();
			return job.getJobId().equals(jobId) && (
					(job.getStartTime().isBefore(currentTime) && job.getEndTime().isAfter(currentTime))
							|| job.getEndTime().equals(currentTime));
		}).findFirst().orElse(null);
	}

	/**
	 * Method verifies if there is only 1 instance of the given job
	 *
	 * @param jobId  unique job identifier
	 * @param jobMap map to traverse
	 * @return boolean
	 */
	public static <T extends PowerJob> boolean isJobUnique(final String jobId, final Map<T, JobStatusEnum> jobMap) {
		return jobMap.keySet().stream().filter(job -> job.getJobId().equals(jobId)).toList().size() == 1;
	}

	/**
	 * Finds distinct start and end times of planned (ACCEPTED_JOB_STATUSES) {@link PowerJob}s including the candidate job
	 *
	 * @param candidateJob job defining the search time window
	 * @param jobMap       job map to traverse
	 * @return list of all start and end times
	 */
	public static <T extends PowerJob> List<Instant> getJobsTimetable(final PowerJob candidateJob,
			final Map<T, JobStatusEnum> jobMap) {
		var validJobs = jobMap.entrySet().stream()
				.filter(entry -> ACCEPTED_JOB_STATUSES.contains(entry.getValue()))
				.map(Map.Entry::getKey)
				.toList();
		return Stream.concat(
						Stream.of(
								convertToRealTime(candidateJob.getStartTime()),
								convertToRealTime(candidateJob.getEndTime())),
						Stream.concat(
								validJobs.stream().map(job -> convertToRealTime(job.getStartTime())),
								validJobs.stream().map(job -> convertToRealTime(job.getEndTime()))))
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
		final Instant endDate = getCurrentTime().isAfter(job.getEndTime()) ? getCurrentTime() : job.getEndTime();
		return Date.from(endDate.plus(MAX_ERROR_IN_JOB_FINISH, ChronoUnit.MILLIS));
	}
}
