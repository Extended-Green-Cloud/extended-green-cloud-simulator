package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static java.util.Collections.emptyList;
import static java.util.Collections.max;
import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.ALLOCATION_PARAMETERS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_PREFERENCES;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TYPES_ENCODING;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TYPES_ENCODING_CLUSTERS;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.SUFFICIENCY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.TYPE;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.PRE_CLUSTERED_RESOURCES;
import static org.greencloud.commons.mapper.ResourceMapper.mapToResourcePreferencesCoefficients;
import static org.greencloud.commons.utils.datastructures.MapConstructor.constructBooleanMap;
import static org.greencloud.commons.utils.math.MathOperations.computeStringSimilarityMatrix;
import static org.greencloud.commons.utils.resources.ResourcesPreferences.constructCoefficientMatrix;
import static org.jrba.utils.mapper.JsonMapper.getMapper;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.domain.resources.ImmutableResourceClustersMatch;
import org.greencloud.commons.domain.resources.ImmutableResourceMatch;
import org.greencloud.commons.domain.resources.ResourceClustersMatch;
import org.greencloud.commons.domain.resources.ResourceMatch;
import org.greencloud.commons.domain.resources.ResourcePreferenceCoefficients;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;
import org.greencloud.commons.exception.InvalidPropertiesException;
import org.greencloud.dataanalysisapi.domain.ClusteringEncodingResponse;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringEncodingResponse;
import org.greencloud.dataanalysisapi.service.DataClusteringService;
import org.greencloud.dataanalysisapi.service.DataClusteringServiceImpl;
import org.jrba.agentmodel.domain.props.AgentProps;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Class with method performing intent based resource allocation
 */
@SuppressWarnings("unchecked")
public class IntentBasedAllocator {

	private static final Logger logger = getLogger(IntentBasedAllocator.class);

	private static final DataClusteringService clusteringService = new DataClusteringServiceImpl();
	private static final int MAX_SIMILARITY_DIFF = 3;

	/**
	 * Method performs intent-based resource allocation.
	 * It follows (modified version) of the algorithms specified in article
	 * [<a href="https://www.sciencedirect.com/science/article/pii/S1110866519303330">Intent-based allocation</a>].
	 *
	 * @param modifications      possible modifications of the algorithm
	 * @param jobsResources      resources (CPU, MEMORY, STORAGE) required for job execution
	 * @param executorsResources resources (CPU, MEMORY, STORAGE) owned by job executors
	 * @param clusterNoJobs      number of desired jobs classes
	 * @param clusterNoExecutors number of desired executors classes
	 * @return mapping between executors identifiers and resource identifiers
	 */
	public static <T extends AgentProps> Map<String, List<String>> intentBasedResourceAllocation(
			final List<AllocationModificationEnum> modifications,
			final List<Map<String, Object>> jobsResources,
			final List<Map<String, Object>> executorsResources, final T properties,
			final int clusterNoJobs, final int clusterNoExecutors) {
		final boolean isPreClustering = modifications.contains(PRE_CLUSTERED_RESOURCES);
		final int jobTypesNumber = isPreClustering ?
				(int) jobsResources.stream().map(map -> (String) map.get(TYPE)).distinct().count() :
				0;
		final int finalJobsClustersNo = isPreClustering ?
				clusteringService.clampClusterNumber(jobTypesNumber, getPredefinedClustersNo(properties)) :
				clusteringService.clampClusterNumber(jobsResources.size(), clusterNoJobs);
		final int finalExecutorClustersNo =
				Optional.of(clusteringService.clampClusterNumber(executorsResources.size(), clusterNoExecutors))
						.filter(clusterNo -> clusterNo >= finalJobsClustersNo)
						.orElseGet(() -> alignExecutorClustersNumberToJobClusters(finalJobsClustersNo));

		if (!properties.getSystemKnowledge().containsKey(RESOURCE_PREFERENCES)) {
			logger.info("To complete this algorithm, system must specify preferences towards resources!");
			throw new InvalidPropertiesException("Resource preferences were not specified.");
		}

		final ResourcePreferenceCoefficients coefficients =
				mapToResourcePreferencesCoefficients(properties.getSystemKnowledge().get(RESOURCE_PREFERENCES));
		final ClusteringEncodingResponse jobsClustering = isPreClustering ?
				prepareClustersBasedOnInitialKnowledge(jobsResources, properties) :
				clusteringService.clusterBasicResourcesAndEncodeDataWithFuzzy(jobsResources, finalJobsClustersNo);
		final ClusteringEncodingResponse executorsClustering =
				mapExecutorsAfterClustering(clusteringService.clusterBasicResourcesAndEncodeDataWithFuzzy(
						mapExecutorsForClustering(executorsResources), finalExecutorClustersNo), executorsResources);
		final Map<String, String> jobsEncoding = jobsClustering.getEncoding();
		final Map<String, String> executorsEncoding = executorsClustering.getEncoding();

		final List<ResourceClustersMatch> executorsClustersAllocation =
				performMatchingBetweenJobAndExecutorClusters(jobsEncoding, executorsEncoding);

		return executorsClustersAllocation.stream()
				.map(clusterMatch -> matchClusterJobsToExecutors(modifications, clusterMatch, executorsClustering,
						jobsClustering, coefficients))
				.flatMap(Collection::stream)
				.collect(groupingBy(ResourceMatch::getExecutorId, mapping(ResourceMatch::getJobId, toList())));

	}

	private static List<ResourceMatch> matchClusterJobsToExecutors(final List<AllocationModificationEnum> modifications,
			final ResourceClustersMatch clusterMatch,
			final ClusteringEncodingResponse executorsClustering,
			final ClusteringEncodingResponse jobsClustering,
			final ResourcePreferenceCoefficients coefficients) {
		final List<Map<String, Object>> executors = ofNullable(
				executorsClustering.getClustering().get(clusterMatch.getExecutorClusterIdx())).orElse(emptyList());
		final List<Map<String, Object>> jobs = ofNullable(
				jobsClustering.getClustering().get(clusterMatch.getJobClusterIdx())).orElse(emptyList());
		final List<List<Double>> coefficientMatrix =
				constructCoefficientMatrix(jobs, executors, coefficients, modifications);

		return range(0, coefficientMatrix.size())
				.filter(jobIndex -> coefficientMatrix.get(jobIndex).stream().allMatch(Objects::nonNull))
				.mapToObj(jobIndex -> Pair.of(jobIndex,
						coefficientMatrix.get(jobIndex).indexOf(max(coefficientMatrix.get(jobIndex)))))
				.map(match -> (ResourceMatch) ImmutableResourceMatch.builder()
						.executorId((String) executors.get(match.getRight()).get(ID))
						.jobId((String) jobs.get(match.getLeft()).get(ID))
						.build())
				.toList();
	}

	private static List<ResourceClustersMatch> performMatchingBetweenJobAndExecutorClusters(
			final Map<String, String> jobsEncoding, final Map<String, String> executorsEncoding) {
		final List<List<Integer>> clusterSimilarityMatrix =
				computeStringSimilarityMatrix(jobsEncoding.values(), executorsEncoding.values());

		final Map<String, AtomicBoolean> jobClustersMatchingStatuses =
				constructBooleanMap(jobsEncoding.keySet(), false);

		final List<ResourceClustersMatch> executorsClustersAllocation = new ArrayList<>();
		final int jobClustersNumber = jobsEncoding.size();

		while (jobClustersMatchingStatuses.values().stream().anyMatch(not(AtomicBoolean::get))) {
			final Map<String, AtomicBoolean> executorClustersAvailability =
					constructBooleanMap(executorsEncoding.keySet(), true);
			final IntStream possibleSimilarityDifference = rangeClosed(0, MAX_SIMILARITY_DIFF);

			possibleSimilarityDifference.forEach(acceptableSimilarity -> {
				final IntStream jobClustersToBeMatched = range(0, jobClustersNumber);

				jobClustersToBeMatched.boxed()
						.filter(jobCluster -> !jobClustersMatchingStatuses.get(jobCluster.toString()).get())
						.filter(jobCluster -> clusterSimilarityMatrix.get(jobCluster).contains(acceptableSimilarity))
						.map(jobCluster -> getMatchingExecutors(jobCluster, acceptableSimilarity,
								clusterSimilarityMatrix.get(jobCluster)))
						.sorted(comparingInt(IntentBasedAllocator::getMatchingExecutorsCount).reversed())
						.forEach(clusterMatching -> {
							final String jobCluster = clusterMatching.getLeft();
							final List<String> matchingExecutorClusters = clusterMatching.getRight();
							matchingExecutorClusters.stream()
									.filter(executorCluster -> executorClustersAvailability.get(executorCluster).get())
									.findFirst()
									.ifPresent(matchingExecutor -> {
										executorClustersAvailability.get(matchingExecutor).set(false);
										jobClustersMatchingStatuses.get(jobCluster).set(true);
										executorsClustersAllocation.add(ImmutableResourceClustersMatch.builder()
												.executorClusterIdx(matchingExecutor)
												.jobClusterIdx(jobCluster)
												.build());
									});
						});
			});
		}
		return executorsClustersAllocation;
	}

	private static <T extends AgentProps> int getPredefinedClustersNo(final T properties) {
		final Map<String, Object> parameters = properties.getSystemKnowledge().get(ALLOCATION_PARAMETERS);
		verifyAllocationParameters(parameters);

		return ((Map<String, String>) parameters.get(TYPES_ENCODING_CLUSTERS)).size();
	}

	private static <T extends AgentProps> ClusteringEncodingResponse prepareClustersBasedOnInitialKnowledge(
			final List<Map<String, Object>> jobsResources, final T properties) {
		final Map<String, Object> parameters = properties.getSystemKnowledge().get(ALLOCATION_PARAMETERS);
		verifyAllocationParameters(parameters);

		final Map<String, String> typesEncoding = (Map<String, String>) parameters.get(TYPES_ENCODING);
		final Map<String, String> jobTypesClusters = (Map<String, String>) parameters.get(TYPES_ENCODING_CLUSTERS);

		return ImmutableClusteringEncodingResponse.builder()
				.clustering(clusteringService.clusterResourcesBasedOnPredefinedTypeLabels(jobsResources, typesEncoding))
				.encoding(jobTypesClusters)
				.build();
	}

	private static List<Map<String, Object>> mapExecutorsForClustering(final List<Map<String, Object>> executors) {
		return executors.stream()
				.map(resourceMap -> resourceMap.entrySet().stream()
						.collect(toMap(Map.Entry::getKey,
								entry -> {
									try {
										return List.of(DURATION, BUDGET, SUFFICIENCY).contains(entry.getKey()) ?
												getMapper().writeValueAsString(entry.getValue()) :
												entry.getValue();
									} catch (final JsonProcessingException e) {
										return entry.getValue();
									}
								})))
				.toList();
	}

	private static ClusteringEncodingResponse mapExecutorsAfterClustering(
			final ClusteringEncodingResponse encodingResponse, final List<Map<String, Object>> executors) {
		final Map<String, List<Map<String, Object>>> clustering = encodingResponse.getClustering().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> mapResourcesAfterClustering(entry.getValue())));

		return ImmutableClusteringEncodingResponse.builder()
				.clustering(clustering)
				.encoding(encodingResponse.getEncoding())
				.build();
	}

	private static List<Map<String, Object>> mapResourcesAfterClustering(final List<Map<String, Object>> resources) {
		return resources.stream()
				.map(clusteringResources -> clusteringResources.entrySet().stream()
						.collect(toMap(Map.Entry::getKey, entry -> {
							final TypeReference<?> typeRef = switch (entry.getKey()) {
								case BUDGET -> new TypeReference<Map<String, Double>>() {
								};
								case DURATION -> new TypeReference<Map<String, Integer>>() {
								};
								case SUFFICIENCY -> new TypeReference<Map<String, Boolean>>() {
								};
								default -> null;
							};
							try {
								return List.of(DURATION, BUDGET, SUFFICIENCY).contains(entry.getKey()) ?
										getMapper().readValue((String) entry.getValue(), typeRef) :
										entry.getValue();
							} catch (final JsonProcessingException e) {
								return entry.getValue();
							}
						})))
				.toList();
	}

	private static int getMatchingExecutorsCount(final Object matchingClusters) {
		return ((Pair<String, List<String>>) matchingClusters).getRight().size();
	}

	private static Pair<String, List<String>> getMatchingExecutors(final int jobClusterIdx,
			final int acceptableSimilarity, final List<Integer> executorSimilarities) {
		final List<String> matchingExecutorIndexes = range(0, executorSimilarities.size())
				.filter(executorSimilarity -> executorSimilarity == acceptableSimilarity).boxed()
				.map(String::valueOf)
				.toList();
		return Pair.of(String.valueOf(jobClusterIdx), matchingExecutorIndexes);
	}

	private static int alignExecutorClustersNumberToJobClusters(final int jobClustersNumber) {
		logger.info("Number of classes for executors must be smaller or equal to the number of "
				+ "classes of jobs. Aligning number of executor clusters to the number of jobs clusters.");

		return jobClustersNumber;
	}

	private static void verifyAllocationParameters(final Map<String, Object> parameters) {
		if (!parameters.containsKey(TYPES_ENCODING) || !parameters.containsKey(TYPES_ENCODING_CLUSTERS)) {
			logger.info("Initial job types encoding must be specified in systems knowledge!");
			throw new InvalidPropertiesException("Job types encoding was not specified.");
		}
	}
}
