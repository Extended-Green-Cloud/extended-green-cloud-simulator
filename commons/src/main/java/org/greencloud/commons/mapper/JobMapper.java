package org.greencloud.commons.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.FROM_GI_TO_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.FROM_MI_TO_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.TO_GI_FROM_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.TO_MI_FROM_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTimeMillis;

import java.time.Instant;

import org.apache.commons.math3.util.Pair;
import org.greencloud.commons.args.job.ImmutableJobArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.args.job.SyntheticJobArgs;
import org.greencloud.commons.args.job.SyntheticJobStepArgs;
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
import org.greencloud.commons.domain.jobstep.ImmutableJobStep;
import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;

import jade.core.AID;

/**
 * Class provides set of methods mapping job object classes
 */
public class JobMapper {

	/**
	 * @param job ClientJob object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapClientJobToJobInstanceId(final ClientJob job) {
		return new ImmutableJobInstanceIdentifier(job.getJobId(), job.getJobInstanceId(),
				requireNonNull(job.getStartTime()));
	}

	/**
	 * @param powerJob PowerJob object
	 * @return JobInstanceIdentifier
	 */
	public static JobInstanceIdentifier mapToJobInstanceId(final PowerJob powerJob) {
		return new ImmutableJobInstanceIdentifier(powerJob.getJobId(), powerJob.getJobInstanceId(),
				requireNonNull(powerJob.getStartTime()));
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
				.requiredResources(job.getRequiredResources())
				.energy(energy)
				.startTime(job.getStartTime())
				.jobSteps(job.getJobSteps())
				.duration(job.getDuration())
				.deadline(job.getDeadline())
				.priority(job.getPriority())
				.build();
	}

	/**
	 * @param job       job to be mapped
	 * @param startTime new start time
	 * @param duration  new duration
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToJobDurationAndStartAndInstanceId(final T job, final Instant startTime,
			final long duration) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.jobInstanceId(job.getJobInstanceId())
						.requiredResources(clientJob.getRequiredResources())
						.selectionPreference(clientJob.getSelectionPreference())
						.deadline(clientJob.getDeadline())
						.jobSteps(clientJob.getJobSteps())
						.priority(job.getPriority())
						.duration(duration)
						.startTime(startTime)
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.jobInstanceId(job.getJobInstanceId())
						.requiredResources(job.getRequiredResources())
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.priority(job.getPriority())
						.duration(duration)
						.startTime(startTime)
						.build();
	}

	/**
	 * @param job         job to be mapped
	 * @param jobInstance new job instance data
	 * @param duration    new job duration
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToJobStartTime(final T job, final JobInstanceIdentifier jobInstance,
			final long duration) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.requiredResources(job.getRequiredResources())
						.startTime(jobInstance.getStartTime())
						.duration(duration)
						.deadline(clientJob.getDeadline())
						.jobSteps(job.getJobSteps())
						.selectionPreference(clientJob.getSelectionPreference())
						.priority(job.getPriority())
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.requiredResources(job.getRequiredResources())
						.startTime(jobInstance.getStartTime())
						.duration(duration)
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.priority(job.getPriority())
						.build();
	}

	/**
	 * @param job       job to be mapped
	 * @param startTime new start time
	 * @param duration  new duration
	 * @return job extending PowerJob
	 */
	@SuppressWarnings("unchecked")
	public static <T extends PowerJob> T mapToJobDurationAndStart(final T job, final Instant startTime,
			final long duration) {
		return job instanceof ClientJob clientJob ?
				(T) ImmutableClientJob.builder()
						.clientIdentifier(clientJob.getClientIdentifier())
						.clientAddress(clientJob.getClientAddress())
						.jobId(clientJob.getJobId())
						.requiredResources(clientJob.getRequiredResources())
						.selectionPreference(clientJob.getSelectionPreference())
						.deadline(clientJob.getDeadline())
						.jobSteps(clientJob.getJobSteps())
						.priority(job.getPriority())
						.duration(duration)
						.startTime(startTime)
						.build() :
				(T) ImmutableServerJob.builder()
						.server(((ServerJob) job).getServer())
						.estimatedEnergy(((ServerJob) job).getEstimatedEnergy())
						.jobId(job.getJobId())
						.requiredResources(job.getRequiredResources())
						.deadline(job.getDeadline())
						.jobSteps(job.getJobSteps())
						.priority(job.getPriority())
						.duration(duration)
						.startTime(startTime)
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
				.requiredResources(serverJob.getRequiredResources())
				.estimatedEnergy(serverJob.getEstimatedEnergy())
				.startTime(convertToRealTime(serverJob.getStartTime()))
				.duration(convertToRealTimeMillis(serverJob.getDuration()))
				.deadline(convertToRealTime(serverJob.getDeadline()))
				.jobSteps(serverJob.getJobSteps())
				.priority(serverJob.getPriority())
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
				.requiredResources(energyJob.getRequiredResources())
				.startTime(energyJob.getStartTime())
				.duration(energyJob.getDuration())
				.deadline(energyJob.getDeadline())
				.jobSteps(energyJob.getJobSteps())
				.priority(energyJob.getPriority())
				.build();
	}

	/**
	 * @param syntheticJobArgs argo job parsed from synthetic workflows
	 * @return job arguments
	 */
	public static JobArgs mapSyntheticArgoJobToJob(final SyntheticJobArgs syntheticJobArgs) {
		final Double cpuInCores = (double) (syntheticJobArgs.getResources().get(CPU)) / syntheticJobArgs.getDuration();
		final Double memoryInMi =
				(double) (syntheticJobArgs.getResources().get(MEMORY) * 100) / syntheticJobArgs.getDuration();
		final Double storageInGi = (double) syntheticJobArgs.getResources().get(STORAGE);

		final Resource cpuResource = ImmutableResource.builder()
				.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
						.value(cpuInCores)
						.unit("cores")
						.build())
				.build();
		final Resource memoryResource = ImmutableResource.builder()
				.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
						.value(memoryInMi)
						.unit("Mi")
						.toCommonUnitConverter(FROM_MI_TO_BYTE_CONVERTER)
						.fromCommonUnitConverter(TO_MI_FROM_BYTE_CONVERTER)
						.build())
				.build();
		final Resource storageResource = ImmutableResource.builder()
				.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
						.value(storageInGi)
						.unit("Gi")
						.toCommonUnitConverter(FROM_GI_TO_BYTE_CONVERTER)
						.fromCommonUnitConverter(TO_GI_FROM_BYTE_CONVERTER)
						.build())
				.build();

		return ImmutableJobArgs.builder()
				.duration(syntheticJobArgs.getDuration())
				.deadline(syntheticJobArgs.getDeadline())
				.processorName(syntheticJobArgs.getProcessorName())
				.putResources(CPU, cpuResource)
				.putResources(MEMORY, memoryResource)
				.putResources(STORAGE, storageResource)
				.priority(syntheticJobArgs.getPriority())
				.jobSteps(syntheticJobArgs.getJobSteps().stream()
						.map(JobMapper::mapSyntheticArgoJobStepToJobStep).toList())
				.build();
	}

	/**
	 * @param syntheticJobStepArgs argo job step parsed from synthetic workflows
	 * @return job arguments
	 */
	public static JobStep mapSyntheticArgoJobStepToJobStep(final SyntheticJobStepArgs syntheticJobStepArgs) {
		final Double cpuInCores = syntheticJobStepArgs.getDuration() == 0 ? 0 :
				(double) (syntheticJobStepArgs.getResources().get(CPU)) / syntheticJobStepArgs.getDuration();
		final Double memoryInMi = syntheticJobStepArgs.getDuration() == 0 ? 0 :
				(double) (syntheticJobStepArgs.getResources().get(MEMORY) * 100) / syntheticJobStepArgs.getDuration();

		final Resource cpuResource = ImmutableResource.builder()
				.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
						.value(cpuInCores)
						.unit("cores")
						.build())
				.build();
		final Resource memoryResource = ImmutableResource.builder()
				.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
						.value(memoryInMi)
						.unit("Mi")
						.toCommonUnitConverter(FROM_MI_TO_BYTE_CONVERTER)
						.fromCommonUnitConverter(TO_MI_FROM_BYTE_CONVERTER)
						.build())
				.build();

		return ImmutableJobStep.builder()
				.name(syntheticJobStepArgs.getName())
				.putRequiredResources(CPU, cpuResource)
				.putRequiredResources(MEMORY, memoryResource)
				.duration(syntheticJobStepArgs.getDuration())
				.build();
	}
}
