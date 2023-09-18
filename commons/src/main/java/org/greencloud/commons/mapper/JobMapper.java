package org.greencloud.commons.mapper;

import static java.util.Objects.isNull;

import java.time.Instant;

import org.apache.commons.math3.util.Pair;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.commons.domain.job.basic.ImmutableClientJob;
import org.greencloud.commons.domain.job.basic.ImmutableEnergyJob;
import org.greencloud.commons.domain.job.basic.ImmutableServerJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.instance.ImmutableJobInstanceIdentifier;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.transfer.ImmutableJobPowerShortageTransfer;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.utils.time.TimeConverter;

import jade.core.AID;

/**
 * Class provides set of methods mapping job object classes
 */
public class JobMapper {

	/**
	 * @param job ClientJob object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final ClientJob job) {
		return new ImmutableJobInstanceIdentifier(job.getJobId(), job.getJobInstanceId(), job.getStartTime());
	}

	/**
	 * @param powerJob PowerJob object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final PowerJob powerJob) {
		return new ImmutableJobInstanceIdentifier(powerJob.getJobId(), powerJob.getJobInstanceId(),
				powerJob.getStartTime());
	}

	/**
	 * @param job       job to be mapped to job
	 * @param endTime   new job end time
	 * @param startTime new job start time
	 * @return ClientJob
	 */
	public static ClientJob mapToJobWithNewTime(final ClientJob job, final Instant startTime, final Instant endTime) {
		return ImmutableClientJob.builder()
				.jobId(job.getJobId())
				.jobInstanceId(job.getJobInstanceId())
				.clientIdentifier(job.getClientIdentifier())
				.clientAddress(job.getClientAddress())
				.estimatedResources(job.getEstimatedResources())
				.startTime(startTime)
				.endTime(endTime)
				.deadline(job.getDeadline())
				.jobSteps(job.getJobSteps())
				.build();
	}

	/**
	 * @param job       job to be mapped to job
	 * @param startTime new job start time
	 * @return ClientJob
	 */
	public static ClientJob mapToJobNewStartTime(final ClientJob job, final Instant startTime) {
		return ImmutableClientJob.builder()
				.clientIdentifier(job.getClientIdentifier())
				.clientAddress(job.getClientAddress())
				.jobId(job.getJobId())
				.jobInstanceId(job.getJobInstanceId())
				.estimatedResources(job.getEstimatedResources())
				.startTime(startTime)
				.endTime(job.getEndTime())
				.deadline(job.getDeadline())
				.jobSteps(job.getJobSteps())
				.build();
	}

	/**
	 * @param job    PowerJob
	 * @param energy energy required for job execution
	 * @return EnergyJob
	 */
	public static EnergyJob mapPowerJobToEnergyJob(final PowerJob job, final double energy) {
		return ImmutableEnergyJob.builder()
				.jobId(job.getJobId())
				.jobInstanceId(job.getJobInstanceId())
				.estimatedResources(job.getEstimatedResources())
				.energy(energy)
				.jobSteps(job.getJobSteps())
				.startTime(job.getStartTime())
				.endTime(job.getEndTime())
				.deadline(job.getDeadline())
				.build();
	}

	/**
	 * @param job       job extending PowerJob that is to be mapped to job
	 * @param startTime new job start time
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToNewJobInstanceStartTime(final T job, final Instant startTime) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.estimatedResources(job.getEstimatedResources())
						.startTime(startTime)
						.endTime(clientJob.getEndTime())
						.deadline(clientJob.getDeadline())
						.jobSteps(clientJob.getJobSteps())
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.estimatedResources(job.getEstimatedResources())
						.startTime(startTime)
						.endTime(job.getEndTime())
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.build();
	}

	/**
	 * @param job     job extending PowerJob to be mapped to job
	 * @param endTime new job end time
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToNewJobInstanceEndTime(final T job, final Instant endTime) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.estimatedResources(job.getEstimatedResources())
						.startTime(clientJob.getStartTime())
						.endTime(endTime)
						.deadline(clientJob.getDeadline())
						.jobSteps(clientJob.getJobSteps())
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.estimatedResources(job.getEstimatedResources())
						.startTime(job.getStartTime())
						.endTime(endTime)
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.build();
	}

	/**
	 * @param job         job to be mapped
	 * @param jobInstance new job instance data
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToJobStartTimeAndInstanceId(final T job,
			final JobInstanceIdentifier jobInstance) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.jobInstanceId(jobInstance.getJobInstanceId())
						.estimatedResources(job.getEstimatedResources())
						.startTime(jobInstance.getStartTime())
						.endTime(clientJob.getEndTime())
						.deadline(clientJob.getDeadline())
						.jobSteps(job.getJobSteps())
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.jobInstanceId(jobInstance.getJobInstanceId())
						.estimatedResources(job.getEstimatedResources())
						.startTime(jobInstance.getStartTime())
						.endTime(job.getEndTime())
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.build();
	}

	/**
	 * @param job           job to be mapped
	 * @param jobInstanceId job instance identifier
	 * @param endTime       new end time
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToJobEndTimeAndInstanceId(final T job,
			final String jobInstanceId, final Instant endTime) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.jobInstanceId(jobInstanceId)
						.estimatedResources(job.getEstimatedResources())
						.startTime(clientJob.getStartTime())
						.endTime(endTime)
						.deadline(clientJob.getDeadline())
						.jobSteps(clientJob.getJobSteps())
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.jobInstanceId(jobInstanceId)
						.estimatedResources(job.getEstimatedResources())
						.startTime(job.getStartTime())
						.endTime(endTime)
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.build();
	}

	/**
	 * @param serverJob server job to be mapped to job with time frames referencing real time
	 * @return ServerJob
	 */
	public static ServerJob mapToServerJobRealTime(final ServerJob serverJob) {
		return ImmutableServerJob.builder()
				.server(serverJob.getServer())
				.jobId(serverJob.getJobId())
				.jobInstanceId(serverJob.getJobInstanceId())
				.estimatedResources(serverJob.getEstimatedResources())
				.estimatedEnergy(serverJob.getEstimatedEnergy())
				.startTime(TimeConverter.convertToRealTime(serverJob.getStartTime()))
				.endTime(TimeConverter.convertToRealTime(serverJob.getEndTime()))
				.deadline(TimeConverter.convertToRealTime(serverJob.getDeadline()))
				.jobSteps(serverJob.getJobSteps())
				.build();
	}

	/**
	 * @param originalJobInstanceId unique identifier of original job
	 * @param jobInstances          pair of job instances
	 * @param startTime             power shortage start time
	 * @return JobPowerShortageTransfer
	 */
	public static <T extends PowerJob> JobPowerShortageTransfer mapToPowerShortageJob(
			final String originalJobInstanceId, final JobDivided<T> jobInstances, final Instant startTime) {
		final Pair<JobInstanceIdentifier, JobInstanceIdentifier> mappedInstances = isNull(
				jobInstances.getFirstInstance()) ?
				new Pair<>(null, mapToJobInstanceId(jobInstances.getSecondInstance())) :
				new Pair<>(mapToJobInstanceId(jobInstances.getFirstInstance()),
						mapToJobInstanceId(jobInstances.getSecondInstance()));
		return new ImmutableJobPowerShortageTransfer(originalJobInstanceId, mappedInstances.getFirst(),
				mappedInstances.getSecond(), startTime);
	}

	/**
	 * @param jobInstance job identifier of job that is to be transferred
	 * @param startTime   power shortage start time
	 * @return JobPowerShortageTransfer
	 */
	public static JobPowerShortageTransfer mapToPowerShortageJob(final JobInstanceIdentifier jobInstance,
			final Instant startTime) {
		return new ImmutableJobPowerShortageTransfer(null, null, jobInstance, startTime);
	}

	/**
	 * @param energyJob energy job
	 * @param server    server that sent given job
	 * @return ServerJob
	 */
	public static ServerJob mapToServerJob(final EnergyJob energyJob, final AID server) {
		return ImmutableServerJob.builder()
				.server(server)
				.estimatedEnergy(energyJob.getEnergy())
				.jobId(energyJob.getJobId())
				.jobInstanceId(energyJob.getJobInstanceId())
				.estimatedResources(energyJob.getEstimatedResources())
				.startTime(energyJob.getStartTime())
				.endTime(energyJob.getEndTime())
				.deadline(energyJob.getDeadline())
				.jobSteps(energyJob.getJobSteps())
				.build();
	}
}
