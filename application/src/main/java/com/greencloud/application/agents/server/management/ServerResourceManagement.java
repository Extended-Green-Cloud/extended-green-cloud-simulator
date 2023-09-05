package com.greencloud.application.agents.server.management;

import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.utils.AlgorithmUtils.getMaximumUsedResourcesDuringTimeStamp;
import static com.greencloud.application.utils.TimeUtils.differenceInHours;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED_BY_SERVER_JOB_STATUSES;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.greencloud.application.agents.AbstractAgentManagement;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.greencloud.commons.domain.resources.ImmutableHardwareResources;

/**
 * Set of utilities used to manage the resources of a given server
 */
public class ServerResourceManagement extends AbstractAgentManagement {

	private final ServerAgent serverAgent;

	public ServerResourceManagement(ServerAgent serverAgent) {
		this.serverAgent = serverAgent;
	}

	/**
	 * Method computes maximal power consumption on given time interval
	 *
	 * @param startDate - time from which max power consumption is to be computed
	 * @param endDate   - time to which max power consumption is to be computed
	 * @return estimated maximal power consumption
	 */
	public synchronized double getPowerConsumption(final Instant startDate, final Instant endDate) {
		final HardwareResources resourceUtilization = getAvailableResources(startDate, endDate, null, null);
		final double cpuInUse = serverAgent.getResources().getCpu() - resourceUtilization.getCpu();
		final double cpuUtilization = cpuInUse / serverAgent.getResources().getCpu();

		return computePowerConsumption(cpuUtilization);
	}

	/**
	 * Method estimates energy required for job execution
	 *
	 * @param job job for which energy is to be estimated
	 * @return energy required to process the job
	 */
	public double estimateEnergyForJob(final ClientJob job) {
		final double cpuUsage = job.getEstimatedResources().getCpu() / serverAgent.getResources().getCpu();
		final double powerConsumption = computePowerConsumption(cpuUsage);

		return powerConsumption * differenceInHours(job.getStartTime(), job.getEndTime());
	}

	/**
	 * Method estimates available resources (of given type) for the specified job.
	 *
	 * @param job          job which time frames are taken into account
	 * @param jobToExclude (optional) job which will be excluded from the resource calculation
	 * @param statusSet    (optional) set of statuses of jobs that are taken into account while calculating in use resources
	 * @return available resources
	 */
	public synchronized HardwareResources getAvailableResources(final ClientJob job,
			final JobInstanceIdentifier jobToExclude,
			final Set<JobExecutionStatusEnum> statusSet) {
		return getAvailableResources(job.getStartTime(), job.getEndTime(), jobToExclude, statusSet);
	}

	/**
	 * Method returns amount of available resources for the specified time frame
	 *
	 * @param startDate    start time
	 * @param endDate      end time
	 * @param jobToExclude (optional) job which will be excluded in resource computation
	 * @param statusSet    (optional) set of statuses of jobs that are taken into account while calculating in use resources
	 * @return available resources
	 */
	public synchronized HardwareResources getAvailableResources(final Instant startDate, final Instant endDate,
			final JobInstanceIdentifier jobToExclude, final Set<JobExecutionStatusEnum> statusSet) {
		final Set<JobExecutionStatusEnum> statuses = isNull(statusSet) ? ACCEPTED_BY_SERVER_JOB_STATUSES : statusSet;
		final Set<ClientJob> jobsOfInterest = serverAgent.getServerJobs().keySet().stream()
				.filter(job -> isNull(jobToExclude) || !mapToJobInstanceId(job).equals(jobToExclude))
				.filter(job -> statuses.contains(serverAgent.getServerJobs().get(job)))
				.collect(toSet());

		final HardwareResources maxResources = getMaximumUsedResourcesDuringTimeStamp(jobsOfInterest, startDate,
				endDate);
		return serverAgent.getResources().computeResourceDifference(maxResources);
	}

	/**
	 * Method computes in-use resources
	 *
	 * @return resources utilization
	 */
	public synchronized HardwareResources getInUseResources() {
		final List<ClientJob> activeJobs = serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> List.of(IN_PROGRESS_BACKUP_ENERGY, IN_PROGRESS).contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(ClientJob.class::cast)
				.toList();
		final double inUseCpu = activeJobs.stream().mapToDouble(job -> job.getEstimatedResources().getCpu()).sum();
		final double inUseMemory = activeJobs.stream().mapToDouble(job -> job.getEstimatedResources().getMemory())
				.sum();
		final double inUseStorage = activeJobs.stream().mapToDouble(job -> job.getEstimatedResources().getStorage())
				.sum();

		return ImmutableHardwareResources.builder().cpu(inUseCpu).memory(inUseMemory).storage(inUseStorage).build();
	}

	/**
	 * Method computes CPU utilization
	 *
	 * @param statusSet (optional) set of statuses taken into account in caclulations
	 * @return CPU utilization
	 */
	public synchronized double getCPUUsage(final Set<JobExecutionStatusEnum> statusSet) {
		final double cpuInUse = serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> (nonNull(statusSet) && statusSet.contains(job.getValue()))
						|| (isNull(statusSet) && job.getValue().equals(IN_PROGRESS)))
				.mapToDouble(job -> job.getKey().getEstimatedResources().getCpu())
				.sum();
		return cpuInUse / serverAgent.getResources().getCpu();
	}

	/**
	 * Method computes current power consumption based on CPU utilization
	 *
	 * @return current power consumption
	 */
	public double getCurrentPowerConsumption() {
		final double cpuUtilization = getCPUUsage(null);
		return computePowerConsumption(cpuUtilization);
	}

	/**
	 * Method computes current power consumption based on CPU utilization
	 *
	 * @return current power consumption
	 */
	public double getCurrentPowerConsumptionBackUp() {
		final double cpuUtilization = getCPUUsage(singleton(IN_PROGRESS_BACKUP_ENERGY));
		return cpuUtilization == 0 ? 0 : computePowerConsumption(cpuUtilization);
	}

	private double computePowerConsumption(final double cpuUtilization) {
		return (serverAgent.getMaxPowerConsumption() - serverAgent.getIdlePowerConsumption()) * cpuUtilization
				+ serverAgent.getIdlePowerConsumption();
	}
}
