package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.agentsystem.strategies.algorithms.priority.PriorityEstimator.evaluatePriorityBasedOnCombinedCredits;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.ALLOCATION_PARAMETERS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TYPES_ENCODING;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TYPES_ENCODING_CLUSTERS;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BASIC_RESOURCES;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ENERGY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.PRIORITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.SUFFICIENCY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.TYPE;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.ENERGY_PREFERENCE;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.PRE_CLUSTERED_RESOURCES;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.RESOURCE_SUFFICIENCY_CHECK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;
import org.greencloud.commons.enums.resources.ResourcePriorityEnum;
import org.greencloud.dataanalysisapi.service.DataClusteringService;
import org.greencloud.dataanalysisapi.service.DataClusteringServiceImpl;
import org.jrba.agentmodel.domain.props.AgentProps;

/**
 * Class with method performing priority-based KMeans allocation
 */
@SuppressWarnings("unchecked")
public class PriorityBasedAllocator {

	private static final int CLUSTER_NO = 3;
	private static final DataClusteringService clusteringService = new DataClusteringServiceImpl();

	/**
	 * Method performs a priority-based allocation that is enhanced with KMeans clustering.
	 * It follows (modified version) of the algorithms specified in article
	 * [<a href="https://www.sciencedirect.com/science/article/pii/S1110866519303330">Priority-based allocation</a>].
	 *
	 * @param jobsToAllocate    jobs that are to be allocated
	 * @param executorResources resources of available executors
	 * @param properties        agent properties
	 * @param modifications     optional algorithm modifications
	 * @return mapping between executors identifiers and resource identifiers
	 */
	public static <T extends AgentProps> Map<String, List<String>> priorityBasedKMeansAllocation(
			final List<ClientJob> jobsToAllocate,
			final List<Map<String, Object>> executorResources,
			final T properties,
			final List<AllocationModificationEnum> modifications) {
		final List<Map<String, Object>> jobResourcesToCluster = jobsToAllocate.stream()
				.map(job -> mapJobPriorityResources(job, jobsToAllocate, executorResources))
				.toList();
		final Map<String, Object> allocationParams = properties.getSystemKnowledge().get(ALLOCATION_PARAMETERS);
		final boolean isPreClustering = modifications.contains(PRE_CLUSTERED_RESOURCES);
		final boolean isWithGreenEnergyPriority = modifications.contains(ENERGY_PREFERENCE);

		final int jobTypesNumber = isPreClustering ?
				(int) jobsToAllocate.stream().map(ClientJob::getJobType).distinct().count() :
				0;
		final int clusterNumber = isPreClustering && jobTypesNumber < 3 ? jobTypesNumber : CLUSTER_NO;

		final Map<String, List<Map<String, Object>>> clusteringJobs = isPreClustering ?
				clusteringService.clusterResourcesBasedOnPredefinedTypeLabels(jobResourcesToCluster,
						(Map<String, String>) allocationParams.get(TYPES_ENCODING), clusterNumber) :
				clusteringService.clusterResourcesWithModifiedKMeans(List.of(PRIORITY), jobResourcesToCluster,
						clusterNumber);
		final Map<String, List<Map<String, Object>>> clusteringExecutors =
				clusteringService.clusterResourcesWithModifiedKMeans(BASIC_RESOURCES, executorResources,
						clusterNumber);

		final Map<ResourcePriorityEnum, String> labeledJobClusters = isPreClustering ?
				labelClustersFromInitial((Map<String, String>) allocationParams.get(TYPES_ENCODING_CLUSTERS)) :
				labelClusters(clusteringJobs, PRIORITY);
		final Map<ResourcePriorityEnum, String> labeledExecutorsClusters = isWithGreenEnergyPriority ?
				labelClusters(clusteringExecutors, ENERGY) :
				labelClusters(clusteringExecutors, CPU);

		return stream(ResourcePriorityEnum.values())
				.map(priority -> mapJobsToExecutors(
						clusteringJobs.getOrDefault(labeledJobClusters.get(priority), emptyList()),
						clusteringExecutors.getOrDefault(labeledExecutorsClusters.get(priority), emptyList()),
						modifications))
				.flatMap(clusterMapping -> clusterMapping.entrySet().stream())
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Map<String, List<String>> mapJobsToExecutors(final List<Map<String, Object>> jobsWithinCluster,
			final List<Map<String, Object>> executorsWithinCluster,
			final List<AllocationModificationEnum> modifications) {
		final Map<String, List<String>> executorsMapping = executorsWithinCluster.stream()
				.collect(toMap(resources -> (String) resources.get(ID), resources -> new ArrayList<>()));
		final AtomicInteger lastExecutor = new AtomicInteger(0);

		jobsWithinCluster.stream()
				.map(resources -> (String) resources.get(ID))
				.forEach(jobId -> {
					final int nextExecutorIndex = modifications.contains(RESOURCE_SUFFICIENCY_CHECK) ?
							selectExecutorWithMatchingResources(jobId, executorsWithinCluster, executorsMapping) :
							lastExecutor.getAndUpdate(index -> index == executorsMapping.size() - 1 ? 0 : index + 1);

					if (nextExecutorIndex != -1) {
						final String selectedExecutor = (String) executorsWithinCluster.get(nextExecutorIndex).get(ID);
						executorsMapping.get(selectedExecutor).add(jobId);
					}
				});

		return executorsMapping;
	}

	private static int selectExecutorWithMatchingResources(final String jobId,
			final List<Map<String, Object>> executors, final Map<String, List<String>> executorsMapping) {
		return IntStream.range(0, executors.size())
				.filter(executorIdx -> ((Map<String, Boolean>) executors.get(executorIdx).get(SUFFICIENCY)).get(jobId))
				.mapToObj(executorIdx -> Pair.of(executorIdx,
						executorsMapping.get((String) executors.get(executorIdx).get(ID)).size()))
				.min(comparingInt(Pair::getValue))
				.map(Pair::getKey)
				.orElse(-1);
	}

	private static Map<String, Object> mapJobPriorityResources(final ClientJob job,
			final List<ClientJob> jobsToAllocate, final List<Map<String, Object>> executorResources) {
		return Map.of(
				ID, job.getJobId(),
				TYPE, job.getJobType(),
				PRIORITY, evaluatePriorityBasedOnCombinedCredits(job, jobsToAllocate, executorResources)
		);
	}

	private static Map<ResourcePriorityEnum, String> labelClustersFromInitial(
			final Map<String, String> initialEncoding) {
		return initialEncoding.entrySet().stream()
				.collect(toMap(entry -> ResourcePriorityEnum.valueOf(entry.getKey()), Map.Entry::getValue));
	}

	private static Map<ResourcePriorityEnum, String> labelClusters(
			final Map<String, List<Map<String, Object>>> clustering, final String feature) {
		final List<String> sortedClusters = clustering.entrySet().stream()
				.sorted(comparingDouble(entry -> entry.getValue().stream()
						.mapToDouble(resources -> (double) resources.get(feature))
						.average()
						.orElse(0.0)))
				.map(Map.Entry::getKey)
				.toList();

		return IntStream.range(0, sortedClusters.size()).boxed()
				.collect(toMap(idx -> ResourcePriorityEnum.values()[idx], sortedClusters::get));
	}
}
