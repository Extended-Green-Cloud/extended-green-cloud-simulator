package org.greencloud.commons.domain.job.duration;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS_JOB_STATUSES;
import static org.greencloud.commons.utils.job.JobUtils.initializeJobStatusDurationMap;
import static org.greencloud.commons.utils.time.TimeConverter.convertMillisToHoursRealTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.extended.JobStatusWithTime;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.enums.job.JobIdentificationEnum;
import org.slf4j.Logger;

import lombok.Getter;
import lombok.Setter;

/**
 * Method represents component used to control duration of execution of the set of jobs
 */
@Getter
@Setter
public class JobExecutionDuration<T extends PowerJob> {

	public static final String NO_JOB_LOG = "No job execution timers were set for job {}.";

	private static final Logger logger = getLogger(JobExecutionDuration.class);

	protected final ConcurrentMap<T, ConcurrentMap<JobExecutionStatusEnum, JobStatusWithTime>> jobsExecutionTime;
	protected final JobIdentificationEnum jobIdentification;

	/**
	 * Default constructor injecting empty status map.
	 */
	public JobExecutionDuration(final JobIdentificationEnum jobIdentification) {
		this.jobsExecutionTime = new ConcurrentHashMap<>();
		this.jobIdentification = jobIdentification;
	}

	/**
	 * Method retrieves duration map for a given job
	 *
	 * @param job job for which map is to be retrieved
	 */
	public ConcurrentMap<JobExecutionStatusEnum, JobStatusWithTime> getForJob(final T job) {
		return jobsExecutionTime.get(job);
	}

	/**
	 * Method adds duration map for a given job
	 *
	 * @param job job for which map is to be added
	 * @param map map that is to be added for given job
	 */
	public void addDurationMap(final T job, final ConcurrentMap<JobExecutionStatusEnum, JobStatusWithTime> map) {
		jobsExecutionTime.put(job, map);
	}

	/**
	 * Method initializes duration map for a given job
	 *
	 * @param job job for which map is to be initialized
	 */
	public void addDurationMap(final T job) {
		jobsExecutionTime.put(job, initializeJobStatusDurationMap());
	}

	/**
	 * Method removes duration map for a given job
	 *
	 * @param job job for which map is to be removes
	 */
	public void removeDurationMap(final T job) {
		jobsExecutionTime.remove(job);
	}

	/**
	 * Method starts the timer for the first status for a given job.
	 *
	 * @param job       job for which the timer is to be started
	 * @param status    status for which timer is to be started
	 * @param startTime start of job
	 */
	public void startJobExecutionTimer(final T job, final JobExecutionStatusEnum status,
			final Instant startTime) {
		if (jobsExecutionTime.containsKey(job)) {
			jobsExecutionTime.get(job).get(status).getTimer().startTimeMeasure(startTime);
		} else {
			logger.info(NO_JOB_LOG, job.getJobInstanceId());
		}
	}

	/**
	 * Method ends the timer for the first status for a given job.
	 *
	 * @param job     job for which the timer is to be started
	 * @param status  status for which timer is to be started
	 * @param endTime end of job
	 */
	public void stopJobExecutionTimer(final T job, final JobExecutionStatusEnum status,
			final Instant endTime) {
		if (jobsExecutionTime.containsKey(job)) {
			final ConcurrentMap<JobExecutionStatusEnum, JobStatusWithTime> executionMap =
					jobsExecutionTime.get(job);
			final long duration = executionMap.get(status).getTimer().getIsStarted().get()
					? executionMap.get(status).getTimer().stopTimeMeasure(endTime)
					: 0;
			executionMap.get(status).getDuration().addAndGet(duration);
		} else {
			logger.info(NO_JOB_LOG, job.getJobInstanceId());
		}
	}

	/**
	 * Method updates the timers for job execution
	 *
	 * @param job            job for which the timers are to be updated
	 * @param previousStatus previous status of the job
	 * @param newStatus      new status of the job
	 * @param changeTime     time when the change of statuses occurred
	 */
	public void updateJobExecutionDuration(final T job, final JobExecutionStatusEnum previousStatus,
			final JobExecutionStatusEnum newStatus, final Instant changeTime) {
		if (jobsExecutionTime.containsKey(job)) {
			stopJobExecutionTimer(job, previousStatus, changeTime);
			startJobExecutionTimer(job, newStatus, changeTime);
		} else {
			logger.info(NO_JOB_LOG, job.getJobInstanceId());
		}
	}

	/**
	 * Method computes price for given job for active statuses
	 *
	 * @param job          job for which price is to be computed
	 * @param unitaryPrice price for single time unit
	 */
	public double computeFinalPrice(final T job, final double unitaryPrice) {
		if (jobsExecutionTime.containsKey(job)) {
			final ConcurrentMap<JobExecutionStatusEnum, JobStatusWithTime> jobDuration = jobsExecutionTime.get(job);
			final long durationInMillis = jobDuration.entrySet().stream()
					.filter(entry -> IN_PROGRESS_JOB_STATUSES.contains(entry.getKey()))
					.map(Map.Entry::getValue)
					.map(JobStatusWithTime::getDuration)
					.map(AtomicLong::get)
					.mapToLong(Long::longValue)
					.sum();
			return unitaryPrice * convertMillisToHoursRealTime(durationInMillis);
		} else {
			logger.info(NO_JOB_LOG, job.getJobInstanceId());
			return 0;
		}
	}

}
