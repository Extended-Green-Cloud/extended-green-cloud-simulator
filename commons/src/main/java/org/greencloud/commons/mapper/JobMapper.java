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
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.math3.util.Pair;
import org.greencloud.commons.args.job.ImmutableJobArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.args.job.SyntheticJobArgs;
import org.greencloud.commons.args.job.SyntheticJobStepArgs;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.allocation.ImmutableAllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.commons.domain.job.basic.ImmutableClientJob;
import org.greencloud.commons.domain.job.basic.ImmutableClientJobWithServer;
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
	 * @param clientJob job that is to mapped with server
	 * @param server    agent identifier of the server
	 * @return ClientJobWithServer
	 */
	public static <T extends ClientJob> ClientJobWithServer mapToJobWithServer(final T clientJob,
			final @Nullable String server) {
		return ImmutableClientJobWithServer.builder()
				.jobId(clientJob.getJobId())
				.ruleSetId(clientJob.getRuleSetId())
				.jobInstanceId(clientJob.getJobInstanceId())
				.duration(clientJob.getDuration())
				.deadline(clientJob.getDeadline())
				.requiredResources(clientJob.getRequiredResources())
				.jobSteps(clientJob.getJobSteps())
				.startTime(clientJob.getStartTime())
				.priority(clientJob.getPriority())
				.jobType(clientJob.getJobType())
				.clientIdentifier(clientJob.getClientIdentifier())
				.clientAddress(clientJob.getClientAddress())
				.selectionPreference(clientJob.getSelectionPreference())
				.server(server)
				.build();

	}

	/**
	 * @param jobWithServer job that is to mapped
	 * @return ClientJobWithServer
	 */
	public static ClientJob mapToClientJob(final ClientJobWithServer jobWithServer) {
		return ImmutableClientJob.builder()
				.jobId(jobWithServer.getJobId())
				.ruleSetId(jobWithServer.getRuleSetId())
				.jobInstanceId(jobWithServer.getJobInstanceId())
				.duration(jobWithServer.getDuration())
				.deadline(jobWithServer.getDeadline())
				.jobType(jobWithServer.getJobType())
				.requiredResources(jobWithServer.getRequiredResources())
				.jobSteps(jobWithServer.getJobSteps())
				.startTime(jobWithServer.getStartTime())
				.priority(jobWithServer.getPriority())
				.clientIdentifier(jobWithServer.getClientIdentifier())
				.clientAddress(jobWithServer.getClientAddress())
				.selectionPreference(jobWithServer.getSelectionPreference())
				.build();

	}

	/**
	 * @param jobs jobs that are to be sent for allocation
	 * @return AllocatedJobs
	 */
	public static <T extends ClientJob> AllocatedJobs mapToAllocatedJobs(final List<T> jobs) {
		return ImmutableAllocatedJobs.builder()
				.allocationJobs(jobs.stream()
						.map(Optional::of)
						.map(job -> job.filter(ClientJobWithServer.class::isInstance)
								.map(ClientJobWithServer.class::cast)
								.orElse(mapToJobWithServer(job.orElseThrow(), null)))
						.toList())
				.build();
	}

	/**
	 * @param acceptedJobs jobs that are to be allocated
	 * @param rejectedJobs jobs which allocation was rejected
	 * @return AllocatedJobs
	 */
	public static <T extends ClientJob> AllocatedJobs mapToAllocatedJobsWithRejection(final List<T> acceptedJobs,
			final List<T> rejectedJobs) {
		return ImmutableAllocatedJobs.builder()
				.allocationJobs(acceptedJobs.stream()
						.map(Optional::of)
						.map(job -> job.filter(ClientJobWithServer.class::isInstance)
								.map(ClientJobWithServer.class::cast)
								.orElse(mapToJobWithServer(job.orElseThrow(), null)))
						.toList())
				.rejectedAllocationJobs(rejectedJobs.stream()
						.map(Optional::of)
						.map(job -> job.filter(ClientJobWithServer.class::isInstance)
								.map(ClientJobWithServer.class::cast)
								.orElse(mapToJobWithServer(job.orElseThrow(), null)))
						.toList())
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
						.jobType(clientJob.getJobType())
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
						.jobType(clientJob.getJobType())
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
						.jobType(clientJob.getJobType())
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
	 * @param budgetLimit      optional budget limit
	 * @return job arguments
	 */
	public static JobArgs mapSyntheticArgoJobToJob(final SyntheticJobArgs syntheticJobArgs,
			@Nullable final Double budgetLimit) {
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
