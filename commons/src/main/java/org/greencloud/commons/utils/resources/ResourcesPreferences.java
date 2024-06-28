package org.greencloud.commons.utils.resources;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.String.valueOf;
import static java.time.Duration.between;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.BooleanUtils.toInteger;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ENERGY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.SUFFICIENCY;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.ENERGY_PREFERENCE;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.RESOURCE_SUFFICIENCY_CHECK;
import static org.greencloud.commons.utils.time.TimeScheduler.computeFinishTime;

import java.time.Instant;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.greencloud.commons.domain.resources.ImmutableResourcePreferenceMatch;
import org.greencloud.commons.domain.resources.ResourcePreferenceCoefficients;
import org.greencloud.commons.domain.resources.ResourcePreferenceMatch;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;

/**
 * Class contains methods computing the preference of resource selection
 *
 * @implNote Formulas used aare from articles:
 * [<a href="https://www.sciencedirect.com/science/article/abs/pii/S0020025520304588">Intent-based allocation</a>],
 * [<a href="https://link.springer.com/article/10.1007/s10586-020-03176-1">Budget-deadline-based allocation</a>]
 */
@SuppressWarnings("unchecked")
public class ResourcesPreferences {

	public static final Random random = new Random();
	private static final Double DEFAULT_RESOURCE_RELIABILITY = 0.8;
	private static final NaturalRanking ranking = new NaturalRanking();

	/**
	 * Method computes the Bi-Factor used in resource allocation.
	 *
	 * @param priority            job priority (rank)
	 * @param executionEstimation estimated execution details of the job
	 * @return Bi-Factor
	 * @implNote following formula from
	 * [<a href="https://link.springer.com/article/10.1007/s10586-020-03176-1">Budget-deadline-based allocation</a>]
	 */
	public static double computeBiFactor(final double priority,
			final JobWithExecutionEstimation executionEstimation,
			final Long optimisticSpareDeadline, final Double optimisticSpareBudget) {
		final long expectedFinishTime = computeFinishTime(executionEstimation).toEpochMilli();
		final double timeFactor = (double) expectedFinishTime /
				(((double) executionEstimation.getEstimatedDuration() / priority)
						* optimisticSpareDeadline);
		final double costFactor = executionEstimation.getEstimatedPrice() / optimisticSpareBudget;

		return timeFactor + costFactor;
	}

	/**
	 * Method computes the deadline factor (TOD) used in resource allocation.
	 *
	 * @param job                  job for which TOD is to be computed
	 * @param priority             job priority (rank)
	 * @param executionEstimations estimated execution details of the job
	 * @return Pair of TOD and optimistic spare deadline
	 * @implNote following formula from
	 * [<a href="https://link.springer.com/article/10.1007/s10586-020-03176-1">Budget-deadline-based allocation</a>]
	 */
	public static Pair<Instant, Long> computeTaskOptimisticDeadline(final ClientJob job, final double priority,
			final List<JobWithExecutionEstimation> executionEstimations) {
		final Instant bestStartTime = executionEstimations.stream()
				.filter(jobEstimation -> nonNull(jobEstimation.getEarliestStartTime()))
				.min(comparing(JobWithExecutionEstimation::getEarliestStartTime))
				.map(JobWithExecutionEstimation::getEarliestStartTime)
				.orElse(job.getDeadline());
		final long minimalExecutionTime = executionEstimations.stream()
				.mapToLong(JobWithExecutionEstimation::getEstimatedDuration)
				.min()
				.orElse(0);

		final long optimisticSpareDeadline = between(job.getDeadline(), bestStartTime).toMillis() - (long) priority;

		return Pair.of(
				bestStartTime.plusMillis(optimisticSpareDeadline + minimalExecutionTime),
				optimisticSpareDeadline
		);
	}

	/**
	 * Method computes the budget factor (TOAB) used in resource allocation.
	 *
	 * @param job                  job for which TOAB is to be computed
	 * @param executionEstimations estimated execution details of the job
	 * @return TOAB
	 * @implNote following formula from
	 * [<a href="https://link.springer.com/article/10.1007/s10586-020-03176-1">Budget-deadline-based allocation</a>]
	 */
	public static Pair<Double, Double> computeTaskOptimisticAvailableBudget(final ClientJob job,
			final List<JobWithExecutionEstimation> executionEstimations) {
		final double maximalPrice = executionEstimations.stream()
				.mapToDouble(JobWithExecutionEstimation::getEstimatedPrice)
				.max()
				.orElse(0.0);
		final double minimalPrice = executionEstimations.stream()
				.mapToDouble(JobWithExecutionEstimation::getEstimatedPrice)
				.min()
				.orElse(0.0);

		final double optimisticSpareBudget = ofNullable(job.getBudgetLimit()).orElse(maximalPrice) - minimalPrice;

		return Pair.of(maximalPrice + optimisticSpareBudget, optimisticSpareBudget);
	}

	/**
	 * Method constructs truncated satisfaction matrix (TSTM) between executors and jobs.
	 *
	 * @param jobs         list of jobs
	 * @param executors    list of executors
	 * @param coefficients coefficients of preferences towards resource matching
	 * @return matrix containing satisfaction coefficients
	 */
	public static List<List<Double>> constructCoefficientMatrix(final List<Map<String, Object>> jobs,
			final List<Map<String, Object>> executors,
			final ResourcePreferenceCoefficients coefficients,
			final List<AllocationModificationEnum> modifications) {
		final int jobsSize = jobs.size();
		return jobs.stream()
				.map(jobResources -> computeMatchingRanks(executors, jobsSize, jobResources, coefficients,
						modifications))
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
			final ResourcePreferenceCoefficients preferenceCoefficients,
			final List<AllocationModificationEnum> modifications) {
		final String jobId = (String) jobResources.get(ID);

		final String budgetLimit = ofNullable(jobResources.get(BUDGET)).map(String::valueOf).orElse(null);
		final Double estimatedExecutionCost = ((Map<String, Double>) executorResources.get(BUDGET)).get(jobId);

		final Double requiredReliability = parseDouble(valueOf(ofNullable(jobResources.get(RELIABILITY))
				.orElse(DEFAULT_RESOURCE_RELIABILITY)));
		final Double resourceSuccessRate = parseDouble(valueOf(executorResources.get(RELIABILITY)));

		final Double requiredEnergyPreference =
				parseDouble(valueOf(ofNullable(jobResources.get(ENERGY)).orElse(0.0)));
		final Double resourceEnergyUtilization = parseDouble(valueOf(executorResources.get(ENERGY)));

		final int expectedDuration = parseInt(valueOf(jobResources.get(DURATION)));
		final Integer estimatedDuration = ((Map<String, Integer>) executorResources.get(DURATION)).get(jobId);

		final boolean resourceSufficiency = ((Map<String, Boolean>) executorResources.get(SUFFICIENCY)).get(jobId);

		final double costPreference = preferenceCoefficients.getCostWeights() *
				determineCostPreference(budgetLimit, estimatedExecutionCost);
		final double reliabilityPreference = preferenceCoefficients.getReliabilityWeight() *
				determineReliabilityPreference(requiredReliability, resourceSuccessRate);
		final double energyPreference = !modifications.contains(ENERGY_PREFERENCE) ? -1 :
				preferenceCoefficients.getEnergyWeight() *
						determineEnergyPreference(requiredEnergyPreference, resourceEnergyUtilization);
		final double timePreference = preferenceCoefficients.getTimeWeight() *
				determineProcessingTimePreference(expectedDuration, estimatedDuration);
		final double performancePreference = preferenceCoefficients.getPerformanceWeight() *
				determineComprehensiveResourcePreference(jobResources, executorResources,
						preferenceCoefficients.getCpuExperienceCoefficient(),
						preferenceCoefficients.getMemoryExperienceCoefficient(),
						preferenceCoefficients.getStorageExperienceCoefficient());

		return modifications.contains(RESOURCE_SUFFICIENCY_CHECK) && !resourceSufficiency ?
				-1 :
				costPreference + reliabilityPreference + timePreference + performancePreference + energyPreference;
	}

	private static List<Double> computeMatchingRanks(final List<Map<String, Object>> executors,
			final int jobsClusterSize,
			final Map<String, Object> jobResources,
			final ResourcePreferenceCoefficients coefficients,
			final List<AllocationModificationEnum> modifications) {
		final int executorsClusterSize = executors.size();

		final List<ResourcePreferenceMatch> matchingPreferences = executors.stream()
				.map(executorResources -> computeBothSidesPreferences(jobResources, executorResources,
						coefficients, modifications))
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
			final Map<String, Object> executorResources,
			final ResourcePreferenceCoefficients coefficients,
			final List<AllocationModificationEnum> modifications) {
		final double executorPreference = computeExecutorMatchingPreference(jobResources, executorResources);
		final double jobPreference =
				computeJobMatchingPreference(jobResources, executorResources, coefficients, modifications);

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

	private static int determineEnergyPreference(final Double requiredEnergyPreference,
			final Double averageEnergyUtilization) {
		return toInteger(averageEnergyUtilization >= requiredEnergyPreference);
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
