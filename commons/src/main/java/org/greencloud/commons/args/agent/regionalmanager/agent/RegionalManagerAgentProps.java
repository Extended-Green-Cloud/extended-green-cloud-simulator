package org.greencloud.commons.args.agent.regionalmanager.agent;

import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.args.agent.regionalmanager.agent.logs.RegionalManagerAgentPropsLog.COUNT_JOB_ACCEPTED_LOG;
import static org.greencloud.commons.args.agent.regionalmanager.agent.logs.RegionalManagerAgentPropsLog.COUNT_JOB_FINISH_LOG;
import static org.greencloud.commons.args.agent.regionalmanager.agent.logs.RegionalManagerAgentPropsLog.COUNT_JOB_PROCESS_LOG;
import static org.greencloud.commons.args.agent.regionalmanager.agent.logs.RegionalManagerAgentPropsLog.COUNT_JOB_START_LOG;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS_JOB_STATUSES;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.computeResourceDifference;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.getInUseResourcesForJobs;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.getMaximumUsedResourcesDuringTimeStamp;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.counter.JobCounter;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Regional Manager Agent
 */
@Getter
@Setter
public class RegionalManagerAgentProps extends EGCSAgentProps {

	private static final Logger logger = getLogger(RegionalManagerAgentProps.class);

	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> networkJobs;
	protected ConcurrentMap<String, Integer> ruleSetForJob;
	protected ConcurrentMap<String, AID> serverForJobMap;
	protected ConcurrentMap<AID, Boolean> ownedServers;
	protected ConcurrentMap<AID, ServerResources> ownedServerResources;
	protected ConcurrentMap<String, Resource> aggregatedResources;
	protected ConcurrentMap<AID, Integer> weightsForServersMap;
	protected ConcurrentMap<AID, Long> highestExecutionTimeErrorForServer;
	protected ConcurrentMap<String, Double> priceForJob;
	protected ConcurrentMap<ClientJob, RuleSetFacts> priorityFacts;
	protected PriorityBlockingQueue<ClientJob> jobsToBeExecuted;
	protected AID cma;

	protected int maximumQueueSize;
	protected int pollingBatchSize;
	protected Instant lastPollingTime;

	public RegionalManagerAgentProps(final String agentName) {
		super(REGIONAL_MANAGER, agentName);
	}

	/**
	 * Constructor that initialize Regional Manager Agent state to initial values
	 *
	 * @param agentName name of the agent
	 */
	public RegionalManagerAgentProps(final String agentName, final int maximumQueueSize, final int pollingBatchSize) {
		super(REGIONAL_MANAGER, agentName);
		this.maximumQueueSize = maximumQueueSize;
		this.pollingBatchSize = pollingBatchSize;
		this.lastPollingTime = getCurrentTime();

		this.serverForJobMap = new ConcurrentHashMap<>();
		this.networkJobs = new ConcurrentHashMap<>();
		this.ownedServers = new ConcurrentHashMap<>();
		this.ownedServerResources = new ConcurrentHashMap<>();
		this.aggregatedResources = new ConcurrentHashMap<>();
		this.ruleSetForJob = new ConcurrentHashMap<>();
		this.weightsForServersMap = new ConcurrentHashMap<>();
		this.priceForJob = new ConcurrentHashMap<>();
		this.priorityFacts = new ConcurrentHashMap<>();
		this.highestExecutionTimeErrorForServer = new ConcurrentHashMap<>();
	}

	/**
	 * Method finds RMA server by name
	 *
	 * @param serverName server name
	 * @return server AID
	 */
	public AID getServerByName(final String serverName) {
		return ownedServers.keySet().stream()
				.filter(serverAID -> serverAID.getName().equals(serverName))
				.findFirst()
				.orElseThrow();
	}

	/**
	 * Method retrieves list of owned servers that are active
	 *
	 * @return list of server AIDs
	 */
	public List<AID> getOwnedActiveServers() {
		return ownedServers.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
	}

	/**
	 * Method updates price of execution of given job
	 *
	 * @param jobId executed job
	 * @param price job execution price
	 * @return updated job price
	 */
	public double updatePriceForJob(final String jobId, final Double price) {
		final double jobExecutionPrice = ofNullable(price).orElse(0D);
		final double newPrice =
				ofNullable(priceForJob.computeIfPresent(jobId, (key, val) -> jobExecutionPrice + val)).orElse(
						jobExecutionPrice);
		priceForJob.putIfAbsent(jobId, jobExecutionPrice);
		return newPrice;
	}

	/**
	 * Method adds new client job
	 *
	 * @param job     job that is to be added
	 * @param ruleSet rule set with which the job is to be handled
	 * @param status  status of the job
	 */
	public void addJob(final ClientJob job, final Integer ruleSet, final JobExecutionStatusEnum status) {
		networkJobs.put(job, status);
		ruleSetForJob.put(job.getJobInstanceId(), ruleSet);
	}

	/**
	 * Method removes client job
	 *
	 * @param job job that is to be removed
	 */
	public int removeJob(final ClientJob job) {
		networkJobs.remove(job);
		priorityFacts.remove(job);
		return ruleSetForJob.remove(job.getJobInstanceId());
	}

	/**
	 * Method estimates available resources (of given type) for the specified job.
	 *
	 * @param job    job which time frames are taken into account
	 * @param server (optional) AID of the server for which resources are to be computed
	 * @return available resources
	 */
	public synchronized Map<String, Resource> getAvailableResources(final ClientJob job, final AID server) {
		return getAvailableResources(getCurrentTime(), getCurrentTime().plusMillis(job.getDuration()), server);
	}

	/**
	 * Method returns amount of available resources for the specified time frame
	 *
	 * @param startDate start time
	 * @param endDate   end time
	 * @param server    (optional) AID of the server for which resources are to be computed
	 * @return available resources
	 */
	public synchronized Map<String, Resource> getAvailableResources(final Instant startDate, final Instant endDate,
			final AID server) {
		final Set<ClientJob> jobs = networkJobs.keySet().stream()
				.filter(job -> isNull(server) || (serverForJobMap.containsKey(job.getJobId()) && serverForJobMap.get(
						job.getJobId()).equals(server)))
				.filter(job -> IN_PROGRESS_JOB_STATUSES.contains(networkJobs.get(job)))
				.collect(toSet());

		final Map<String, Resource> resources = isNull(server) ?
				aggregatedResources :
				getOwnedServerResources().get(server).getResources();
		final Map<String, Resource> maxResources =
				getMaximumUsedResourcesDuringTimeStamp(jobs, resources, startDate, endDate);
		return computeResourceDifference(resources, maxResources);
	}

	/**
	 * Method selects servers with sufficient resources for job execution
	 *
	 * @param job job that is to be executed
	 * @return list of server AIDs
	 */
	public List<AID> selectServersForJob(final ClientJob job) {
		return getOwnedActiveServers().stream()
				.filter(server -> {
					final Map<String, Resource> availableResources = getAvailableResources(job, server);
					return areSufficient(availableResources, job.getRequiredResources());
				})
				.toList();
	}

	/**
	 * Method initializes priority queue
	 */
	public void setUpPriorityQueue(final int maxQueueSize, final ToDoubleFunction<ClientJob> getJobPriority) {
		this.jobsToBeExecuted = new PriorityBlockingQueue<>(maxQueueSize, comparingDouble(getJobPriority));
	}

	/**
	 * Method returns currently used resources
	 *
	 * @return in use resources
	 */
	public Map<String, Resource> getInUseResources() {
		final List<ClientJob> executedJobs = networkJobs.entrySet().stream()
				.filter(job -> job.getValue().equals(IN_PROGRESS))
				.map(Map.Entry::getKey)
				.map(ClientJob.class::cast)
				.toList();
		return getInUseResourcesForJobs(executedJobs, aggregatedResources);
	}

	/**
	 * Method removes unused resources from aggregation
	 */
	public void removeUnusedResources() {
		final Set<String> availableResources = ownedServerResources.values().stream()
				.map(resource -> resource.getResources().keySet().stream().toList()).flatMap(Collection::stream)
				.collect(Collectors.toSet());
		final Set<String> resourcesToRemove = SetUtils.difference(aggregatedResources.keySet(), availableResources);
		resourcesToRemove.forEach(resourceKey -> aggregatedResources.remove(resourceKey));
	}

	/**
	 * Method removes unused resource characteristics from aggregation
	 */
	public void removeUnusedResourceCharacteristics() {
		final Map<String, Resource> updatedResources = aggregatedResources.entrySet().stream().map(entry -> {
			final String key = entry.getKey();
			final Resource aggregatedResource = entry.getValue();
			final Set<String> availableResourceCharacteristics = ownedServerResources.values().stream()
					.map(resource -> resource.getResources().entrySet().stream().toList()).flatMap(Collection::stream)
					.filter(resource -> resource.getKey().equals(key)).map(Map.Entry::getValue)
					.map(resource -> resource.getCharacteristics().keySet().stream().toList())
					.flatMap(Collection::stream).collect(Collectors.toSet());

			final Set<String> characteristicsToRemove = SetUtils.difference(
					aggregatedResource.getCharacteristics().keySet(), availableResourceCharacteristics);
			final Map<String, ResourceCharacteristic> newCharacteristics = new HashMap<>(
					aggregatedResource.getCharacteristics());
			final Map<String, ResourceCharacteristic> newEmptyResourceCharacteristics =
					new HashMap<>(aggregatedResource.getEmptyResource().getCharacteristics());

			characteristicsToRemove.forEach(resourceKey -> {
				newEmptyResourceCharacteristics.remove(resourceKey);
				newCharacteristics.remove(resourceKey);
			});

			final Resource newEmptyResource = ImmutableResource.copyOf(aggregatedResource.getEmptyResource())
					.withCharacteristics(newEmptyResourceCharacteristics);

			return Pair.of(key, ImmutableResource.copyOf(aggregatedResource)
					.withCharacteristics(newCharacteristics)
					.withEmptyResource(newEmptyResource));
		}).collect(toMap(Pair::getKey, Pair::getValue));
		setAggregatedResources(new ConcurrentHashMap<>(updatedResources));
	}

	/**
	 * Method adds new resource characteristics to aggregation
	 */
	public void addResourceCharacteristics(final Map<String, Resource> resourceMap) {
		final Map<String, Resource> updatedResources = aggregatedResources.entrySet().stream()
				.filter(entry -> resourceMap.containsKey(entry.getKey())).map(entry -> {
					final String key = entry.getKey();
					final Resource resource = entry.getValue();
					final Set<String> availableCharacteristics = resource.getCharacteristics().keySet();
					final Set<String> characteristicsToAdd = SetUtils.difference(
							resourceMap.get(key).getCharacteristics().keySet(), availableCharacteristics);

					final Map<String, ResourceCharacteristic> newCharacteristics =
							new HashMap<>(resource.getCharacteristics());
					final Map<String, ResourceCharacteristic> newEmptyResourceCharacteristics =
							new HashMap<>(resource.getEmptyResource().getCharacteristics());

					characteristicsToAdd.forEach(resourceKey -> {
						final Resource resourceForKey = resourceMap.get(key).getEmptyResource();
						newCharacteristics.put(resourceKey, resourceForKey.getCharacteristics().get(resourceKey));
						newEmptyResourceCharacteristics.put(resourceKey,
								resourceForKey.getEmptyResource().getCharacteristics().get(resourceKey));
					});

					final Resource newEmptyResource = ImmutableResource.copyOf(resource.getEmptyResource())
							.withCharacteristics(newEmptyResourceCharacteristics);

					return Pair.of(key, ImmutableResource.copyOf(resource)
							.withCharacteristics(newCharacteristics)
							.withEmptyResource(newEmptyResource));
				}).collect(toMap(Pair::getKey, Pair::getValue));
		setAggregatedResources(new ConcurrentHashMap<>(updatedResources));
	}

	@Override
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>(Map.of(FAILED,
				new JobCounter(jobId -> logger.info(COUNT_JOB_PROCESS_LOG, jobCounters.get(FAILED).getCount())),
				ACCEPTED,
				new JobCounter(jobId -> logger.info(COUNT_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED).getCount())),
				STARTED, new JobCounter(
						jobId -> logger.info(COUNT_JOB_START_LOG, jobId, jobCounters.get(STARTED).getCount(),
								jobCounters.get(ACCEPTED).getCount())), FINISH, new JobCounter(
						jobId -> logger.info(COUNT_JOB_FINISH_LOG, jobId, jobCounters.get(FINISH).getCount(),
								jobCounters.get(STARTED).getCount()))));
	}

	@Override
	public void updateGUI() {
		super.updateGUI();
		saveMonitoringData();
	}
}
