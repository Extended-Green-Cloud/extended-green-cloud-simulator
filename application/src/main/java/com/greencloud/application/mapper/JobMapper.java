package com.greencloud.application.mapper;

import java.time.Instant;

import com.greencloud.application.domain.job.ImmutableJob;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.ImmutablePowerJob;
import com.greencloud.application.domain.job.Job;
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
	public static PowerJob mapJobToPowerJob(final Job job) {
		return ImmutablePowerJob.builder()
				.jobId(job.getJobId())
				.power(job.getPower())
				.startTime(job.getStartTime())
				.endTime(job.getEndTime())
				.build();
	}

	/**
	 * @param powerJob power job to be mapped to job
	 * @return PowerJob
	 */
	public static PowerJob mapToPowerJob(final Job powerJob, final Instant startTime) {
		return ImmutablePowerJob.builder()
				.jobId(powerJob.getJobId())
				.power(powerJob.getPower())
				.startTime(startTime)
				.endTime(powerJob.getEndTime())
				.build();
	}

	/**
	 * @param job       job to be mapped to job
	 * @param startTime new job start time
	 * @return Job
	 */
	public static Job mapToJobNewStartTime(final Job job, final Instant startTime) {
		return ImmutableJob.builder()
				.clientIdentifier(job.getClientIdentifier())
				.jobId(job.getJobId())
				.power(job.getPower())
				.startTime(startTime)
				.endTime(job.getEndTime())
				.build();
	}

	/**
	 * @param job job to be mapped to job
	 * @return Job
	 */
	public static Job mapToJobNewEndTime(final Job job, final Instant endTime) {
		return ImmutableJob.builder()
				.clientIdentifier(job.getClientIdentifier())
				.jobId(job.getJobId())
				.power(job.getPower())
				.startTime(job.getStartTime())
				.endTime(endTime)
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
				.build();
	}

	/**
	 * @param job       job to be mapped to job
	 * @param endTime   new job end time
	 * @param startTime new job start time
	 * @return PowerJob
	 */
	public static Job mapToJobWithNewTime(final Job job, final Instant startTime, final Instant endTime) {
		return ImmutableJob.builder()
				.jobId(job.getJobId())
				.clientIdentifier(job.getClientIdentifier())
				.power(job.getPower())
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}

	/**
	 * @param powerJob PowerJob object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final PowerJob powerJob) {
		return ImmutableJobInstanceIdentifier.builder().jobId(powerJob.getJobId()).startTime(powerJob.getStartTime())
				.build();
	}

	/**
	 * @param job       job to map
	 * @param startTime power shortage start time
	 * @return PowerShortageJob
	 */
	public static PowerShortageJob mapToPowerShortageJob(final Job job, final Instant startTime) {
		return ImmutablePowerShortageJob.builder()
				.jobInstanceId(mapToJobInstanceId(job))
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
	public static JobInstanceIdentifier mapToJobInstanceId(final Job job) {
		return ImmutableJobInstanceIdentifier.builder().jobId(job.getJobId()).startTime(job.getStartTime()).build();
	}
}
