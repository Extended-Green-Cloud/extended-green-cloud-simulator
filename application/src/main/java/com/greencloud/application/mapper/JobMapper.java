package com.greencloud.application.mapper;

import static com.greencloud.application.utils.TimeUtils.convertToRealTime;

import java.time.Instant;

import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.ImmutableClientJob;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.ImmutablePowerJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.domain.powershortage.ImmutablePowerShortageJob;
import com.greencloud.application.domain.powershortage.PowerShortageJob;

/**
 * Class provides set of methods mapping job classes
 */
public class JobMapper {

	/**
	 * @param job job to be mapped to power job
	 * @return PowerJob
	 */
	public static PowerJob mapJobToPowerJob(final ClientJob job) {
		return ImmutablePowerJob.builder()
				.jobId(job.getJobId())
				.power(job.getPower())
				.startTime(job.getStartTime())
				.endTime(job.getEndTime())
				.deadline(job.getDeadline())
				.build();
	}

	/**
	 * @param powerJob power job to be mapped to job
	 * @return PowerJob
	 */
	public static PowerJob mapToPowerJob(final ClientJob powerJob, final Instant startTime) {
		return ImmutablePowerJob.builder()
				.jobId(powerJob.getJobId())
				.power(powerJob.getPower())
				.startTime(startTime)
				.endTime(powerJob.getEndTime())
				.deadline(powerJob.getDeadline())
				.build();
	}

	/**
	 * @param powerJob power job to be mapped to job
	 * @return PowerJob
	 */
	public static PowerJob mapToPowerJobRealTime(final PowerJob powerJob) {
		return ImmutablePowerJob.builder()
				.jobId(powerJob.getJobId())
				.power(powerJob.getPower())
				.startTime(convertToRealTime(powerJob.getStartTime()))
				.endTime(convertToRealTime(powerJob.getEndTime()))
				.deadline(convertToRealTime(powerJob.getDeadline()))
				.build();
	}

	/**
	 * @param job       job to be mapped to job
	 * @param startTime new job start time
	 * @return Job
	 */
	public static ClientJob mapToJobNewStartTime(final ClientJob job, final Instant startTime) {
		return ImmutableClientJob.builder()
				.clientIdentifier(job.getClientIdentifier())
				.jobId(job.getJobId())
				.power(job.getPower())
				.startTime(startTime)
				.endTime(job.getEndTime())
				.deadline(job.getDeadline())
				.build();
	}

	/**
	 * @param job job to be mapped to job
	 * @return Job
	 */
	public static ClientJob mapToJobNewEndTime(final ClientJob job, final Instant endTime) {
		return ImmutableClientJob.builder()
				.clientIdentifier(job.getClientIdentifier())
				.jobId(job.getJobId())
				.power(job.getPower())
				.startTime(job.getStartTime())
				.endTime(endTime)
				.deadline(job.getDeadline())
				.build();
	}

	/**
	 * @param powerJob  power job to be mapped to job
	 * @param startTime new power job start time
	 * @return PowerJob
	 */
	public static PowerJob mapToJobNewStartTime(final PowerJob powerJob, final Instant startTime) {
		return ImmutablePowerJob.builder()
				.jobId(powerJob.getJobId())
				.power(powerJob.getPower())
				.startTime(startTime)
				.endTime(powerJob.getEndTime())
				.deadline(powerJob.getDeadline())
				.build();
	}

	/**
	 * @param powerJob power job to be mapped to job
	 * @param endTime  new power job end time
	 * @return PowerJob
	 */
	public static PowerJob mapToJobNewEndTime(final PowerJob powerJob, final Instant endTime) {
		return ImmutablePowerJob.builder()
				.jobId(powerJob.getJobId())
				.power(powerJob.getPower())
				.startTime(powerJob.getStartTime())
				.endTime(endTime)
				.deadline(powerJob.getDeadline())
				.build();
	}

	/**
	 * @param job       job to be mapped to job
	 * @param endTime   new job end time
	 * @param startTime new job start time
	 * @return PowerJob
	 */
	public static ClientJob mapToJobWithNewTime(final ClientJob job, final Instant startTime, final Instant endTime) {
		return ImmutableClientJob.builder()
				.jobId(job.getJobId())
				.clientIdentifier(job.getClientIdentifier())
				.power(job.getPower())
				.startTime(startTime)
				.endTime(endTime)
				.deadline(job.getDeadline())
				.build();
	}

	/**
	 * @param powerJob PowerJob object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final PowerJob powerJob) {
		return ImmutableJobInstanceIdentifier.builder()
				.jobId(powerJob.getJobId())
				.startTime(powerJob.getStartTime())
				.build();
	}

	/**
	 * @param jobInstanceId JobInstanceIdentifier object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceIdWithRealTime(final JobInstanceIdentifier jobInstanceId) {
		return ImmutableJobInstanceIdentifier.builder()
				.jobId(jobInstanceId.getJobId())
				.startTime(convertToRealTime(jobInstanceId.getStartTime()))
				.build();
	}

	/**
	 * @param job       job to map
	 * @param startTime power shortage start time
	 * @return PowerShortageJob
	 */
	public static PowerShortageJob mapToPowerShortageJob(final ClientJob job, final Instant startTime) {
		return ImmutablePowerShortageJob.builder()
				.jobInstanceId(mapToJobInstanceId(job))
				.powerShortageStart(startTime)
				.build();
	}

	/**
	 * @param jobInstanceId job identifier
	 * @param startTime     power shortage start time
	 * @return PowerShortageJob
	 */
	public static PowerShortageJob mapToPowerShortageJob(final JobInstanceIdentifier jobInstanceId,
			final Instant startTime) {
		return ImmutablePowerShortageJob.builder()
				.jobInstanceId(jobInstanceId)
				.powerShortageStart(startTime)
				.build();
	}

	/**
	 * @param job       power job to map
	 * @param startTime power shortage start time
	 * @return PowerShortageJob
	 */
	public static PowerShortageJob mapToPowerShortageJob(final PowerJob job, final Instant startTime) {
		return ImmutablePowerShortageJob.builder()
				.jobInstanceId(mapToJobInstanceId(job))
				.powerShortageStart(startTime)
				.build();
	}

	/**
	 * @param job Job object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final ClientJob job) {
		return ImmutableJobInstanceIdentifier.builder().jobId(job.getJobId()).startTime(job.getStartTime()).build();
	}

	/**
	 * @param jobInstanceId job instance
	 * @param startTime     job instance start time
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final JobInstanceIdentifier jobInstanceId,
			final Instant startTime) {
		return ImmutableJobInstanceIdentifier.builder()
				.jobId(jobInstanceId.getJobId())
				.startTime(startTime)
				.build();
	}

	/**
	 * @param jobId     job identifier
	 * @param startTime job instance start time
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final String jobId, final Instant startTime) {
		return ImmutableJobInstanceIdentifier.builder()
				.jobId(jobId)
				.startTime(startTime)
				.build();
	}
}
