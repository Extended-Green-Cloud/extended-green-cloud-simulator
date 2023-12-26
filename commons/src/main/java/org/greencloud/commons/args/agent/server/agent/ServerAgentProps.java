package org.greencloud.commons.args.agent.server.agent;

import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.greencloud.commons.args.agent.AgentType.SERVER;
import static org.greencloud.commons.args.agent.server.agent.logs.ServerAgentPropsLog.COUNT_JOB_ACCEPTED_LOG;
import static org.greencloud.commons.args.agent.server.agent.logs.ServerAgentPropsLog.COUNT_JOB_FINISH_LOG;
import static org.greencloud.commons.args.agent.server.agent.logs.ServerAgentPropsLog.COUNT_JOB_PROCESS_LOG;
import static org.greencloud.commons.args.agent.server.agent.logs.ServerAgentPropsLog.COUNT_JOB_START_LOG;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_BY_SERVER_JOB_STATUSES;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static org.greencloud.commons.enums.job.JobIdentificationEnum.JOB_INSTANCE_ID;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.computeResourceDifference;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.getInUseResourcesForJobs;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.getMaximumUsedResourcesDuringTimeStamp;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.greencloud.commons.args.agent.egcs.agent.EGCSAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.counter.JobCounter;
import org.greencloud.commons.domain.job.duration.JobExecutionDuration;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.mapper.JobMapper;
import org.slf4j.Logger;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Arguments representing internal properties of Server Agent
 */
@Getter
@Setter
public class ServerAgentProps extends EGCSAgentProps {

	private static final Logger logger = getLogger(ServerAgentProps.class);

	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> serverJobs;
	protected ConcurrentMap<ClientJob, Integer> ruleSetForJob;
	protected ConcurrentMap<String, AID> greenSourceForJobMap;
	protected AtomicLong currentlyProcessing;
	protected ConcurrentMap<AID, Integer> weightsForGreenSourcesMap;
	protected ConcurrentMap<AID, Boolean> ownedGreenSources;
	protected ConcurrentMap<String, Double> energyExecutionCost;
	protected ConcurrentMap<String, Double> serverPriceForJob;
	protected ConcurrentMap<String, Double> totalPriceForJob;
	protected JobExecutionDuration<ClientJob> jobsExecutionTime;
	protected AID ownerRegionalManagerAgent;

	@Accessors(fluent = true)
	protected Map<String, Resource> resources;
	protected Integer maxPowerConsumption;
	protected Integer idlePowerConsumption;
	protected double pricePerHour;
	protected int jobProcessingLimit;

	protected boolean isDisabled;
	protected boolean hasError;

	public ServerAgentProps(final String agentName) {
		super(SERVER, agentName);
	}

	/**
	 * Constructor
	 *
	 * @param agentName              name of the agent
	 * @param ownerRegionalManagerAgent cloud network to which the server is connected
	 * @param resources              hardware resources owned by the server
	 * @param maxPowerConsumption    maximal power consumption that can be reached by the server
	 * @param idlePowerConsumption   power consumption of a server when no jobs are running
	 * @param pricePerHour           price of job execution calculated for an hour
	 * @param jobProcessingLimit     limit of job requests that a server can process in at once
	 */
	public ServerAgentProps(final String agentName, final AID ownerRegionalManagerAgent,
			final Map<String, Resource> resources,
			final Integer maxPowerConsumption, final Integer idlePowerConsumption, final double pricePerHour,
			final int jobProcessingLimit) {
		this(agentName);
		this.ownerRegionalManagerAgent = ownerRegionalManagerAgent;
		this.resources = new HashMap<>(resources);
		this.maxPowerConsumption = maxPowerConsumption;
		this.idlePowerConsumption = idlePowerConsumption;
		this.pricePerHour = pricePerHour;
		this.jobProcessingLimit = jobProcessingLimit;

		this.serverJobs = new ConcurrentHashMap<>();
		this.ruleSetForJob = new ConcurrentHashMap<>();
		this.ownedGreenSources = new ConcurrentHashMap<>();
		this.greenSourceForJobMap = new ConcurrentHashMap<>();
		this.weightsForGreenSourcesMap = new ConcurrentHashMap<>();
		this.energyExecutionCost = new ConcurrentHashMap<>();
		this.serverPriceForJob = new ConcurrentHashMap<>();
		this.totalPriceForJob = new ConcurrentHashMap<>();

		this.jobsExecutionTime = new JobExecutionDuration<>(JOB_INSTANCE_ID);
		this.currentlyProcessing = new AtomicLong(0L);
		this.hasError = false;
		this.isDisabled = false;
	}

	/**
	 * Method adds new client job
	 *
	 * @param job     job that is to be added
	 * @param ruleSet rule set with which the job is to be handled
	 * @param status  status of the job
	 */
	public void addJob(final ClientJob job, final Integer ruleSet, final JobExecutionStatusEnum status) {
		serverJobs.put(job, status);
		jobsExecutionTime.addDurationMap(job);
		ruleSetForJob.put(job, ruleSet);
	}

	/**
	 * Method removes client job
	 *
	 * @param job job that is to be removed
	 */
	public int removeJob(final ClientJob job) {
		serverJobs.remove(job);
		serverPriceForJob.remove(job.getJobInstanceId());
		energyExecutionCost.remove(job.getJobInstanceId());
		jobsExecutionTime.removeDurationMap(job);
		return ruleSetForJob.remove(job);
	}

	/**
	 * Method updates the energy cost of the given job
	 *
	 * @param jobInstanceWithPrice job that is to be updated
	 */
	public void updateJobEnergyCost(final JobInstanceWithPrice jobInstanceWithPrice) {
		final String jobInstanceId = jobInstanceWithPrice.getJobInstanceId().getJobInstanceId();

		energyExecutionCost.computeIfPresent(jobInstanceId,
				(key, value) -> value + jobInstanceWithPrice.getPrice());
		energyExecutionCost.putIfAbsent(jobInstanceId, jobInstanceWithPrice.getPrice());
	}

	/**
	 * Method computes execution cost of given job (server + green energy)
	 *
	 * @param job job for which cost is to be computed
	 */
	public void updateJobExecutionCost(final ClientJob job) {
		final double energyCost = energyExecutionCost.getOrDefault(job.getJobInstanceId(), 0D);
		final double price = serverPriceForJob.getOrDefault(job.getJobInstanceId(), 0D);
		final double totalCost = energyCost + jobsExecutionTime.computeFinalPrice(job, price);

		totalPriceForJob.computeIfPresent(job.getJobId(), (key, val) -> val + totalCost);
		totalPriceForJob.putIfAbsent(job.getJobId(), totalCost);
	}

	/**
	 * Method connects new green sources to the server agent
	 *
	 * @param newGreenSources list of green sources to connect to the server
	 */
	public void connectNewGreenSourcesToServer(final List<AID> newGreenSources) {
		final Map<AID, Boolean> greenSourceWithState = newGreenSources.stream().collect(toMap(gs -> gs, gs -> true));
		ownedGreenSources.putAll(greenSourceWithState);
		assignWeightsToNewGreenSources(newGreenSources);
	}

	/**
	 * Method computes CPU utilization
	 *
	 * @param statusSet (optional) set of statuses taken into account in caclulations
	 * @return CPU utilization
	 */
	public synchronized double getCPUUsage(final Set<JobExecutionStatusEnum> statusSet) {
		final double cpuInUse = serverJobs.entrySet().stream()
				.filter(job -> (nonNull(statusSet) && statusSet.contains(job.getValue()))
						|| (isNull(statusSet) && job.getValue().equals(IN_PROGRESS)))
				.mapToDouble(job -> job.getKey().getRequiredResources().get(CPU).getAmountInCommonUnit())
				.sum();
		return cpuInUse / resources.get(CPU).getAmountInCommonUnit();
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

	/**
	 * Method estimates energy required for job execution
	 *
	 * @param job job for which energy is to be estimated
	 * @return energy required to process the job
	 */
	public double estimatePowerForJob(final ClientJob job) {
		final double cpuUsage =
				job.getRequiredResources().get(CPU).getAmountInCommonUnit() / resources.get(CPU)
						.getAmountInCommonUnit();
		return computePowerConsumption(cpuUsage);
	}

	/**
	 * Method computes maximal power consumption on given time interval
	 *
	 * @param startDate - time from which max power consumption is to be computed
	 * @param endDate   - time to which max power consumption is to be computed
	 * @return estimated maximal power consumption
	 */
	public synchronized double getPowerConsumption(final Instant startDate, final Instant endDate) {
		final Map<String, Resource> resourceUtilization = getAvailableResources(startDate, endDate, null, null);
		final double cpuInUse =
				resources.get(CPU).getAmountInCommonUnit() - resourceUtilization.get(CPU).getAmountInCommonUnit();
		final double cpuUtilization = cpuInUse / resources.get(CPU).getAmountInCommonUnit();

		return computePowerConsumption(cpuUtilization);
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
	public synchronized Map<String, Resource> getAvailableResources(final Instant startDate, final Instant endDate,
			final JobInstanceIdentifier jobToExclude, final Set<JobExecutionStatusEnum> statusSet) {
		final Set<JobExecutionStatusEnum> statuses = isNull(statusSet) ? ACCEPTED_BY_SERVER_JOB_STATUSES : statusSet;
		final Set<ClientJob> jobs = serverJobs.keySet().stream()
				.filter(job -> isNull(jobToExclude) || !JobMapper.mapClientJobToJobInstanceId(job).equals(jobToExclude))
				.filter(job -> statuses.contains(serverJobs.get(job)))
				.collect(toSet());

		final Map<String, Resource> maxResources =
				getMaximumUsedResourcesDuringTimeStamp(jobs, resources, startDate, endDate);
		return computeResourceDifference(resources, maxResources);
	}

	/**
	 * Method estimates available resources (of given type) for the specified job.
	 *
	 * @param job          job which time frames are taken into account
	 * @param jobToExclude (optional) job which will be excluded from the resource calculation
	 * @param statusSet    (optional) set of statuses of jobs that are taken into account while calculating in use resources
	 * @return available resources
	 */
	public synchronized Map<String, Resource> getAvailableResources(final ClientJob job,
			final JobInstanceIdentifier jobToExclude,
			final Set<JobExecutionStatusEnum> statusSet) {
		return getAvailableResources(job.getStartTime(), job.getEndTime(), jobToExclude, statusSet);
	}

	/**
	 * Method retrieves the addresses of green sources that are marked as active
	 *
	 * @return set of active green sources
	 */
	public Set<AID> getOwnedActiveGreenSources() {
		return ownedGreenSources.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(toSet());
	}

	/**
	 * Method computes in-use resources
	 *
	 * @return resources utilization
	 */
	public synchronized Map<String, Resource> getInUseResources() {
		final List<ClientJob> activeJobs = serverJobs.entrySet().stream()
				.filter(job -> List.of(IN_PROGRESS_BACKUP_ENERGY, IN_PROGRESS).contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(ClientJob.class::cast)
				.toList();
		return getInUseResourcesForJobs(activeJobs, resources);
	}

	/**
	 * Method creates new instances for given server job that will be affected by the internal server error and executes
	 * the post job division handler.
	 *
	 * @param job                job that is to be divided into instances
	 * @param powerShortageStart time when the server failure will start
	 * @return facts with Pair consisting of previous job instance and job instance for transfer (if there is only job instance
	 * * for transfer then previous job instance element is null)
	 */
	public RuleSetFacts divideJobForTransfer(final ClientJob job, final Instant powerShortageStart,
			final RuleSetFacts facts) {
		return super.divideJobForPowerShortage(job, powerShortageStart, serverJobs, facts, ruleSetForJob);
	}

	/**
	 * Method substitutes existing job instance with new instances associated with internal server error job transfer
	 *
	 * @param jobTransfer job transfer information
	 * @param originalJob original job that is to be divided
	 * @return facts about job after division
	 */
	public RuleSetFacts divideJobForTransfer(final JobPowerShortageTransfer jobTransfer,
			final ClientJob originalJob, final RuleSetFacts facts) {
		return super.divideJobForPowerShortage(jobTransfer, originalJob, serverJobs, facts, ruleSetForJob);
	}

	@Override
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>(Map.of(
				FAILED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_PROCESS_LOG, jobCounters.get(FAILED).getCount())),
				ACCEPTED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED).getCount())),
				STARTED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_START_LOG, jobId, jobCounters.get(STARTED).getCount(),
								jobCounters.get(ACCEPTED).getCount())),
				FINISH, new JobCounter(jobId ->
						logger.info(COUNT_JOB_FINISH_LOG, jobId, jobCounters.get(FINISH).getCount(),
								jobCounters.get(STARTED).getCount()))
		));
	}

	/**
	 * Method evaluating if new job can be taken into processing
	 *
	 * @return boolean indicating if server can process new job
	 */
	public boolean canTakeIntoProcessing() {
		return currentlyProcessing.get() < jobProcessingLimit && !isDisabled && !hasError;
	}

	/**
	 * Method increments number of processed jobs
	 */
	public void takeJobIntoProcessing() {
		currentlyProcessing.incrementAndGet();
	}

	/**
	 * Method decrements number of processed jobs
	 */
	public void stoppedJobProcessing() {
		currentlyProcessing.decrementAndGet();
	}

	/**
	 * Method turns off the server
	 */
	public void disable() {
		isDisabled = true;
	}

	/**
	 * Method turns on the server
	 */
	public void enable() {
		isDisabled = false;
	}

	private void assignWeightsToNewGreenSources(final List<AID> newGreenSources) {
		final int maxWeight = weightsForGreenSourcesMap.values().stream().max(Integer::compare).orElse(1);
		final int weight = weightsForGreenSourcesMap.isEmpty() ? 1 : maxWeight;

		newGreenSources.forEach(greenSource -> weightsForGreenSourcesMap.putIfAbsent(greenSource, weight));
	}

	private double computePowerConsumption(final double cpuUtilization) {
		return (maxPowerConsumption - idlePowerConsumption) * cpuUtilization + idlePowerConsumption;
	}

}
