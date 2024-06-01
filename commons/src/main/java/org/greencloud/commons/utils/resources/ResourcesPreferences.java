package org.greencloud.commons.utils.resources;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.BooleanUtils.toInteger;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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
@SuppressWarnings("unchecked")
public class ResourcesPreferences {

	public static final Random random = new Random();
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
		final String jobId = (String) jobResources.get(ID);

		final Double requiredReliability = parseDouble(valueOf(ofNullable(jobResources.get(RELIABILITY))
				.orElse(DEFAULT_RESOURCE_RELIABILITY)));
		final Double estimatedExecutionCost = ((Map<String, Double>) executorResources.get(BUDGET)).get(jobId);
		final Integer estimatedDuration = ((Map<String, Integer>) executorResources.get(DURATION)).get(jobId);

		return ofNullable(jobResources.get(BUDGET))
				.map(limit -> (parseDouble(valueOf(limit)) * estimatedDuration) / requiredReliability)
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
		final String jobId = (String) jobResources.get(ID);

		final String budgetLimit = ofNullable(jobResources.get(BUDGET)).map(String::valueOf).orElse(null);
		final Double estimatedExecutionCost = ((Map<String, Double>) executorResources.get(BUDGET)).get(jobId);

		final Double requiredReliability = parseDouble(valueOf(ofNullable(jobResources.get(RELIABILITY))
				.orElse(DEFAULT_RESOURCE_RELIABILITY)));
		final Double resourceSuccessRate = parseDouble(valueOf(executorResources.get(RELIABILITY)));

		final int expectedDuration = parseInt(valueOf(jobResources.get(DURATION)));
		final Integer estimatedDuration = ((Map<String, Integer>) executorResources.get(DURATION)).get(jobId);

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

		final int minJobSatisfaction =
				clampMinimalSatisfaction(jobsClusterSize, coefficients.getMinimalJobSatisfaction());
		final int minExecutorSatisfaction =
				clampMinimalSatisfaction(executorsClusterSize, coefficients.getMinimalExecutorSatisfaction());

		final double minimalJobSatisfaction = computeSatisfaction(minJobSatisfaction, jobsClusterSize);
		final double minimalExecutorSatisfaction = computeSatisfaction(minExecutorSatisfaction, executorsClusterSize);

		return range(0, executorsClusterSize).mapToObj(
				index -> {
					final Double jobSatisfaction = computeTruncatedSatisfaction(jobsRanks[index],
							jobsClusterSize, minimalJobSatisfaction);
					final Double executorSatisfaction = computeTruncatedSatisfaction(executorsRanks[index],
							executorsClusterSize, minimalExecutorSatisfaction);

					if (Stream.of(jobSatisfaction, executorSatisfaction).allMatch(Objects::nonNull))
						return coefficients.jobSatisfactionWeight() * jobSatisfaction +
								coefficients.executorSatisfactionWeight() * executorSatisfaction;
					return -1.0;
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
		final double satisfaction = computeSatisfaction(rank, clusterSize);
		return satisfaction >= minimalSatisfaction ? satisfaction : null;
	}

	private static double computeSatisfaction(final double rank, final int clusterSize) {
		return pow((clusterSize + 1 - rank) / clusterSize, 2);
	}

	private static int clampMinimalSatisfaction(final int clusterSize, final Double minimalInitialSatisfaction) {
		final ValueRange minExecutorSatisfactionRange = ValueRange.of(clusterSize, (long) clusterSize + 1);
		final int minExecutorCoeff = minimalInitialSatisfaction.intValue();

		return minExecutorSatisfactionRange.isValidIntValue(minExecutorCoeff) ?
				minExecutorCoeff :
				random.nextInt(clusterSize, clusterSize + 1) + clusterSize;
	}

	private static double determineCostPreference(final String budgetLimit, final Double estimatedExecutionCost) {
		return ofNullable(budgetLimit)
				.map(Double::parseDouble)
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

	private static double determineComprehensiveResourcePreference(final Map<String, Object> jobResources,
			final Map<String, Object> executorResources,
			final double cpuExperienceCoeff,
			final double memoryExperienceCoeff,
			final double storageExperienceCoeff) {
		final double jobResourcePreference = computeBasicComprehensiveResourcePreference(jobResources,
				cpuExperienceCoeff, memoryExperienceCoeff, storageExperienceCoeff);
		final double executorResourcePreference = computeBasicComprehensiveResourcePreference(executorResources,
				cpuExperienceCoeff, memoryExperienceCoeff, storageExperienceCoeff);

		return Optional.of(executorResourcePreference)
				.filter(executorPreference -> executorPreference >= jobResourcePreference)
				.map(executorPreference -> (executorPreference - jobResourcePreference) / executorPreference)
				.orElse(0.0D);
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
