package org.greencloud.commons.utils.resources;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.BooleanUtils.toInteger;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.greencloud.commons.domain.resources.ImmutableResourcePreferenceMatch;
import org.greencloud.commons.domain.resources.ResourcePreferenceCoefficients;
import org.greencloud.commons.domain.resources.ResourcePreferenceMatch;

/**
 * Class contains methods computing the preference of resource selection
 *
 * @implNote Formulas used from articles:
 * [<a href="https://www.sciencedirect.com/science/article/abs/pii/S0020025520304588">Intent-based allocation</a>]
 */
public class ResourcesPreferences {

	private static final Double DEFAULT_RESOURCE_RELIABILITY = 0.8;
	private static final NaturalRanking ranking = new NaturalRanking();

	/**
	 * Method constructs truncated satisfaction matrix (TSTM) between executors and jobs.
	 *
	 * @param jobs         list of jobs
	 * @param executors    list of executors
	 * @param coefficients coefficients of preferences towards resource matching
	 * @return matrix containing satisfaction coefficients
	 */
	public static List<List<Double>> constructCoefficientMatrix(final List<Map<String, Object>> jobs,
			final List<Map<String, Object>> executors, final ResourcePreferenceCoefficients coefficients) {
		final int jobsSize = jobs.size();
		return jobs.stream()
				.map(jobResources -> computeMatchingRanks(executors, jobsSize, jobResources, coefficients))
				.toList();
	}

	/**
	 * Method computes the execution preference of given executor for job matching.
	 *
	 * @param jobResources      required job resources
	 * @param executorResources owned executor resources
	 * @return computation preference
	 */
	public static double computeExecutorMatchingPreference(final Map<String, Object> jobResources,
			final Map<String, Object> executorResources) {
		final Double budgetLimit = (Double) jobResources.get(BUDGET);
		final Double requiredReliability = ofNullable((Double) jobResources.get(RELIABILITY))
				.orElse(DEFAULT_RESOURCE_RELIABILITY);
		final Double estimatedExecutionCost = (Double) executorResources.get(BUDGET);
		final Long estimatedDuration = (Long) executorResources.get(DURATION);

		return ofNullable(budgetLimit)
				.map(limit -> (limit * estimatedDuration) / requiredReliability)
				.orElse(estimatedExecutionCost / requiredReliability);
	}

	/**
	 * Method computes the execution preference of given job for executor matching.
	 *
	 * @param jobResources           required job resources
	 * @param executorResources      owned executor resources
	 * @param preferenceCoefficients preferences towards resource matching
	 * @return computation preference
	 */
	public static double computeJobMatchingPreference(final Map<String, Object> jobResources,
			final Map<String, Object> executorResources,
			final ResourcePreferenceCoefficients preferenceCoefficients) {
		final Double budgetLimit = (Double) jobResources.get(BUDGET);
		final Double estimatedExecutionCost = (Double) executorResources.get(BUDGET);

		final Double requiredReliability = ofNullable((Double) jobResources.get(RELIABILITY))
				.orElse(DEFAULT_RESOURCE_RELIABILITY);
		final Double resourceSuccessRate = (Double) executorResources.get(RELIABILITY);

		final Long expectedDuration = (Long) jobResources.get(DURATION);
		final Long estimatedDuration = (Long) executorResources.get(DURATION);

		final double costPreference = preferenceCoefficients.getCostWeights() *
				determineCostPreference(budgetLimit, estimatedExecutionCost);
		final double reliabilityPreference = preferenceCoefficients.getReliabilityWeight() *
				determineReliabilityPreference(requiredReliability, resourceSuccessRate);
		final double timePreference = preferenceCoefficients.getTimeWeight() *
				determineProcessingTimePreference(expectedDuration, estimatedDuration);
		final double performancePreference = preferenceCoefficients.getPerformanceWeight() *
				determineComprehensiveResourcePreference(jobResources, executorResources,
						preferenceCoefficients.getCpuExperienceCoefficient(),
						preferenceCoefficients.getMemoryExperienceCoefficient(),
						preferenceCoefficients.getStorageExperienceCoefficient());

		return costPreference + reliabilityPreference + timePreference + performancePreference;
	}

	private static List<Double> computeMatchingRanks(final List<Map<String, Object>> executors,
			final int jobsClusterSize, final Map<String, Object> jobResources,
			final ResourcePreferenceCoefficients coefficients) {
		final int executorsClusterSize = executors.size();

		final List<ResourcePreferenceMatch> matchingPreferences = executors.stream()
				.map(executorResources -> computeBothSidesPreferences(jobResources, executorResources, coefficients))
				.toList();
		final double[] executorsRanks = ranking.rank(matchingPreferences.stream()
				.mapToDouble(ResourcePreferenceMatch::getExecutorPreference).toArray());
		final double[] jobsRanks = ranking.rank(matchingPreferences.stream()
				.mapToDouble(ResourcePreferenceMatch::getJobPreference).toArray());

		final double minimalJobSatisfaction =
				computeSatisfaction(jobsClusterSize * 0.05, jobsClusterSize);
		final double minimalExecutorSatisfaction =
				computeSatisfaction(executorsClusterSize * 0.05, jobsClusterSize);

		return range(0, executorsClusterSize).mapToObj(
				index -> {
					final Double jobSatisfaction = computeTruncatedSatisfaction(jobsRanks[index],
							jobsClusterSize, minimalJobSatisfaction);
					final Double executorSatisfaction = computeTruncatedSatisfaction(executorsRanks[index],
							executorsClusterSize, minimalExecutorSatisfaction);

					if (Stream.of(jobSatisfaction, executorSatisfaction).allMatch(Objects::nonNull))
						return coefficients.jobSatisfactionWeight() * jobSatisfaction +
								coefficients.executorSatisfactionWeight() * executorSatisfaction;
					return null;
				}).toList();
	}

	private static ResourcePreferenceMatch computeBothSidesPreferences(final Map<String, Object> jobResources,
			final Map<String, Object> executorResources, final ResourcePreferenceCoefficients coefficients) {
		final double executorPreference = computeExecutorMatchingPreference(jobResources, executorResources);
		final double jobPreference = computeJobMatchingPreference(jobResources, executorResources, coefficients);

		return ImmutableResourcePreferenceMatch.builder()
				.executorPreference(executorPreference)
				.jobPreference(jobPreference)
				.build();
	}

	private static Double computeTruncatedSatisfaction(final double rank, final int clusterSize,
			final double minimalSatisfaction) {
		final double satisfaction = sqrt((clusterSize + 1 - rank) / clusterSize);
		return satisfaction >= minimalSatisfaction ? satisfaction : null;
	}

	private static double computeSatisfaction(final double rank, final int clusterSize) {
		return sqrt((clusterSize + 1 - rank) / clusterSize);
	}

	private static double determineCostPreference(final Double budgetLimit, final Double estimatedExecutionCost) {
		return ofNullable(budgetLimit)
				.filter(limit -> limit >= estimatedExecutionCost)
				.map(limit -> (limit - estimatedExecutionCost) / limit)
				.orElse(0D);
	}

	private static int determineReliabilityPreference(final Double requiredReliability,
			final Double executorSuccessRate) {
		return toInteger(executorSuccessRate >= requiredReliability);
	}

	private static int determineProcessingTimePreference(final long expectedDuration,
			final long estimatedTaskDuration) {
		return toInteger(expectedDuration >= estimatedTaskDuration);
	}

	private static int determineComprehensiveResourcePreference(final Map<String, Object> jobResources,
			final Map<String, Object> executorResources,
			final double cpuExperienceCoeff,
			final double memoryExperienceCoeff,
			final double storageExperienceCoeff) {
		final double jobResourcePreference = computeBasicComprehensiveResourcePreference(jobResources,
				cpuExperienceCoeff, memoryExperienceCoeff, storageExperienceCoeff);
		final double executorResourcePreference = computeBasicComprehensiveResourcePreference(executorResources,
				cpuExperienceCoeff, memoryExperienceCoeff, storageExperienceCoeff);

		return toInteger(executorResourcePreference >= jobResourcePreference);
	}

	private static double computeBasicComprehensiveResourcePreference(final Map<String, Object> resources,
			final double cpuExperienceCoeff, final double memoryExperienceCoeff, final double storageExperienceCoeff) {
		final double cpuAmountSquared = pow((double) resources.get(CPU), 2);
		final double memoryAmountSquared = pow((double) resources.get(MEMORY), 2);
		final double storageAmountSquared = pow((double) resources.get(STORAGE), 2);

		return sqrt((cpuAmountSquared * cpuExperienceCoeff +
				memoryAmountSquared * memoryExperienceCoeff +
				storageAmountSquared * storageExperienceCoeff) /
				(cpuExperienceCoeff + memoryExperienceCoeff + storageExperienceCoeff));
	}
}
