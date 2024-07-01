package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.util.Collections.emptyMap;
import static java.util.Collections.max;
import static java.util.Collections.nCopies;
import static java.util.Collections.shuffle;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.SetUtils.difference;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.utils.datastructures.MapConstructor.constructListMap;
import static org.greencloud.commons.utils.datastructures.MapConstructor.constructMapWithMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.greencloud.commons.domain.allocation.AntAllocationParameters;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Class with method performing aco-based resource allocation
 */
public class AntColonyBasedAllocator {

	private static final int INITIAL_PHEROMONE = 0;

	/**
	 * Method performs a ACO-based resource allocation.
	 * It follows (modified version) of the algorithm specified in article
	 * [<a href="https://ieeexplore.ieee.org/document/6707172">ACO-based allocation</a>].
	 *
	 * @param antParams          parameters associated with ACO algorithm
	 * @param executorsResources available resources of executors
	 * @param modifications      possible modifications of the algorithm
	 * @param jobs               jobs for which the executors are to be allocated
	 * @return mapping between executors identifiers and resource identifiers
	 */
	public static Map<String, List<String>> acoBasedAllocation(
			final AntAllocationParameters antParams,
			final List<ClientJob> jobs,
			final List<Map<String, Object>> executorsResources,
			final List<AllocationModificationEnum> modifications
	) {
		final int executorsNumber = executorsResources.size();
		final int jobsNumber = jobs.size();
		final int antsNumberFixed = min(antParams.getAntsNumber(), executorsNumber);
		final List<Integer> executorIndexes = IntStream.range(0, executorsNumber).boxed().toList();

		final List<List<Double>> executorsVisibility = computeExecutorsVisibility(jobs, executorsResources);
		final List<List<AtomicDouble>> globalPheromones = initializePheromones(jobsNumber, executorsNumber);

		final AtomicDouble bestTourValue = new AtomicDouble(0);
		final AtomicInteger bestTourIdx = new AtomicInteger(0);
		final AtomicReference<Map<Integer, Integer>> bestTour = new AtomicReference<>(emptyMap());

		IntStream.range(0, antParams.getMaxIterations()).forEach(iteration -> {
			final Map<Integer, List<Integer>> antsPath = initializeAntsPath(executorIndexes, antsNumberFixed);
			final Map<Integer, List<Integer>> jobsPerExecutor = constructListMap(executorIndexes);
			final Map<Integer, Map<Integer, Integer>> antsExecutorSelection = constructMapWithMap(antsPath.keySet());

			performExecutorSelection(antsPath, jobsNumber, antParams, executorIndexes, executorsVisibility,
					globalPheromones, antsExecutorSelection, jobsPerExecutor);
			performPheromoneUpdate(antsPath, jobsNumber, executorsNumber, antParams, executorsVisibility,
					jobsPerExecutor, globalPheromones, antsExecutorSelection, bestTourIdx, bestTourValue);

			bestTour.set(antsExecutorSelection.get(bestTourIdx.get()));
			performGlobalPheromoneUpdate(bestTour.get(), antParams, globalPheromones, executorsVisibility);
		});

		return bestTour.get().entrySet().stream()
				.collect(toMap(entry -> (String) executorsResources.get(entry.getKey()).get(ID),
						entry -> singletonList(jobs.get(entry.getKey()).getJobId())));
	}

	private static void performExecutorSelection(final Map<Integer, List<Integer>> antsPath,
			final int jobsNumber,
			final AntAllocationParameters antParams,
			final List<Integer> executorIndexes,
			final List<List<Double>> executorsVisibility,
			final List<List<AtomicDouble>> globalPheromones,
			final Map<Integer, Map<Integer, Integer>> antsExecutorSelection,
			final Map<Integer, List<Integer>> jobsPerExecutor) {
		final Set<Integer> ants = antsPath.keySet();

		ants.forEach(antIdx -> {
			final Set<Integer> allExecutors = new HashSet<>(executorIndexes);

			IntStream.range(0, jobsNumber).forEach(jobIdx -> {
				final Set<Integer> traversedExecutors = new HashSet<>(antsPath.get(antIdx));
				final Set<Integer> availableExecutors = difference(allExecutors, traversedExecutors);

				final List<Double> probabilities = computeTransitionProbabilities(availableExecutors, jobIdx,
						globalPheromones, antParams, executorsVisibility);
				final int bestProbabilityIdx = probabilities.indexOf(max(probabilities));
				final int selectedExecutor = availableExecutors.stream().toList().get(bestProbabilityIdx);

				antsPath.get(antIdx).add(selectedExecutor);
				antsExecutorSelection.get(antIdx).put(selectedExecutor, jobIdx);
				jobsPerExecutor.get(selectedExecutor).add(jobIdx);
			});
		});
	}

	private static void performGlobalPheromoneUpdate(final Map<Integer, Integer> bestAntTour,
			final AntAllocationParameters antParams,
			final List<List<AtomicDouble>> globalPheromones,
			final List<List<Double>> executorsVisibility) {
		final double traversedLength = bestAntTour.entrySet().stream()
				.mapToDouble(entry -> executorsVisibility.get(entry.getValue()).get(entry.getKey()))
				.sum();
		final double update = antParams.getAdaptiveParam() / traversedLength;

		bestAntTour.forEach((executorIdx, jobIdx) ->
				globalPheromones.get(jobIdx).get(executorIdx)
						.updateAndGet(currentPheromone -> currentPheromone + update));
	}

	private static void performPheromoneUpdate(final Map<Integer, List<Integer>> antsPath,
			final int jobsNumber,
			final int executorsNumber,
			final AntAllocationParameters antParams,
			final List<List<Double>> executorsVisibility,
			final Map<Integer, List<Integer>> jobsPerExecutor,
			final List<List<AtomicDouble>> globalPheromones,
			final Map<Integer, Map<Integer, Integer>> antsExecutorSelection,
			final AtomicInteger bestTourIdx,
			final AtomicDouble bestTourValue) {
		final Set<Integer> ants = antsPath.keySet();
		final List<List<AtomicDouble>> pheromoneUpdate = initializePheromones(jobsNumber, executorsNumber);

		ants.forEach(antIdx -> {
			final List<Integer> traversedExecutors = antsPath.get(antIdx);
			final List<Double> traversedLengthPerExecutor =
					computeTraversedTrialLengths(traversedExecutors, jobsPerExecutor, executorsVisibility);
			final double maxLength = max(traversedLengthPerExecutor);
			final double antPheromone = antParams.getAdaptiveParam() / maxLength;

			Optional.of(antPheromone)
					.filter(pheromone -> pheromone > bestTourValue.get())
					.ifPresent(pheromone -> {
						bestTourValue.set(pheromone);
						bestTourIdx.set(antIdx);
					});

			antsExecutorSelection.get(antIdx).forEach((executorIdx, jobIdx) ->
					pheromoneUpdate.get(jobIdx).get(executorIdx).updateAndGet(pheromone -> pheromone + antPheromone));
		});
		updateLocalPheromones(jobsNumber, executorsNumber, globalPheromones, pheromoneUpdate, antParams);
	}

	private static void updateLocalPheromones(final int jobsNumber, final int executorsNumber,
			final List<List<AtomicDouble>> globalPheromones,
			final List<List<AtomicDouble>> pheromoneUpdate,
			final AntAllocationParameters antParams) {

		IntStream.range(0, jobsNumber).forEach(jobIdx ->
				IntStream.range(0, executorsNumber).forEach(executorIdx -> {
					final double newPheromone = pheromoneUpdate.get(jobIdx).get(executorIdx).get();
					globalPheromones.get(jobIdx).get(executorIdx).updateAndGet(currentPheromone ->
							computeNewPheromoneValue(newPheromone, antParams.getTrialDecay(), currentPheromone));
				}));
	}

	private static List<List<AtomicDouble>> initializePheromones(final int jobsNumber, final int executorsNumber) {
		return new ArrayList<>(nCopies(jobsNumber, new ArrayList<>(nCopies(executorsNumber,
				new AtomicDouble(INITIAL_PHEROMONE)))));
	}

	private static Map<Integer, List<Integer>> initializeAntsPath(final List<Integer> executorIndexes,
			final int antsNumber) {
		final List<Integer> indexes = new ArrayList<>(executorIndexes);
		shuffle(indexes);

		return IntStream.range(0, antsNumber).boxed()
				.collect(toMap(antIdx -> antIdx, antIdx -> new ArrayList<>(singletonList(indexes.get(antIdx)))));
	}

	private static List<Double> computeTraversedTrialLengths(final List<Integer> traversedExecutors,
			final Map<Integer, List<Integer>> jobsPerExecutor,
			final List<List<Double>> executorsVisibility) {
		return traversedExecutors.stream()
				.map(executorIdx -> jobsPerExecutor.get(executorIdx).stream()
						.mapToDouble(jobIdx -> executorsVisibility.get(jobIdx).get(executorIdx))
						.sum())
				.toList();
	}

	private static List<Double> computeTransitionProbabilities(final Set<Integer> availableExecutors,
			final int jobIdx,
			final List<List<AtomicDouble>> globalPheromones,
			final AntAllocationParameters antParams,
			final List<List<Double>> executorsVisibility) {
		return availableExecutors.stream()
				.mapToDouble(executorIdx -> computeTransitionProbability(
						globalPheromones.get(jobIdx).get(executorIdx).get(),
						executorsVisibility.get(jobIdx).get(executorIdx),
						antParams.getAlphaWeight(),
						antParams.getBetaWeight())
				).boxed().toList();
	}

	private static List<List<Double>> computeExecutorsVisibility(final List<ClientJob> jobs,
			final List<Map<String, Object>> executorsResources) {
		return jobs.stream()
				.map(job -> executorsResources.stream()
						.map(executor -> computeHeuristics(job, executor))
						.toList())
				.toList();
	}

	private static double computeNewPheromoneValue(final double pheromoneUpdate, final double trialDecay,
			final double currentPheromone) {
		return (1 - trialDecay) * currentPheromone + pheromoneUpdate;
	}

	private static double computeTransitionProbability(final Double currentPheromone, final Double visibility,
			final double alpha, final double beta) {
		return pow(currentPheromone, alpha) * pow((1 / visibility), beta);
	}

	private static double computeHeuristics(final ClientJob job, final Map<String, Object> executor) {
		return (job.getDuration() / ((double) executor.get(CU) * (double) executor.get(CPU)));
	}
}
