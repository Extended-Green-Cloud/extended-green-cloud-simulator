package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.RESOURCE_SUFFICIENCY_CHECK;
import static org.greencloud.commons.utils.resources.ResourcesPreferences.computeBiFactor;
import static org.greencloud.commons.utils.resources.ResourcesPreferences.computeTaskOptimisticAvailableBudget;
import static org.greencloud.commons.utils.resources.ResourcesPreferences.computeTaskOptimisticDeadline;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.greencloud.commons.utils.time.TimeScheduler.computeFinishTime;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;

/**
 * Class with method performing budget-deadline constrained resource allocation
 */
public class BudgetDeadlineAllocator {

	/**
	 * Method performs a budget-deadline constrained allocation.
	 * It follows (modified version) of the algorithm specified in article
	 * [<a href="https://link.springer.com/article/10.1007/s10586-020-03176-1">Budget-deadline constrained allocation</a>].
	 *
	 * @param job                  job that are to be allocated
	 * @param priority             assigned job priority
	 * @param executorsResources   available resources of executors
	 * @param modifications        possible modifications of the algorithm
	 * @param executorsEstimations estimations of job execution
	 * @return mapping between executors identifiers and resource identifiers
	 */
	public static Map<String, List<String>> budgetDeadlineConstrainedAllocation(
			final ClientJob job, final double priority,
			final List<AllocationModificationEnum> modifications,
			final Map<String, ServerResources> executorsResources,
			final Map<String, JobWithExecutionEstimation> executorsEstimations) {
		final Map<String, JobWithExecutionEstimation> executorsToConsider = executorsEstimations.entrySet().stream()
				.filter(executor -> !modifications.contains(RESOURCE_SUFFICIENCY_CHECK) ||
						areSufficient(executorsResources.get(executor.getKey()).getResources(),
								job.getRequiredResources()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		final List<JobWithExecutionEstimation> jobEstimations = executorsToConsider.values().stream().toList();

		final Pair<Instant, Long> jobOptimisticDeadline = computeTaskOptimisticDeadline(job, priority, jobEstimations);
		final Pair<Double, Double> jobOptimisticAvailableBudget =
				computeTaskOptimisticAvailableBudget(job, jobEstimations);

		final Map<String, JobWithExecutionEstimation> affordableExecutors = executorsToConsider.entrySet().stream()
				.filter(executor -> executor.getValue().getEstimatedPrice() < jobOptimisticAvailableBudget.getKey())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		final String selectedExecutor = Optional.of(affordableExecutors)
				.filter(not(Map::isEmpty))
				.map(executors -> selectAffordableExecutor(priority,
						affordableExecutors,
						jobOptimisticDeadline.getKey(),
						jobOptimisticDeadline.getValue(),
						jobOptimisticAvailableBudget.getValue()
				))
				.orElseGet(() -> selectNonAffordableExecutor(executorsToConsider, jobOptimisticDeadline.getKey()));

		return selectedExecutor.equals(EMPTY) ?
				emptyMap() :
				Map.of(selectedExecutor, singletonList(job.getJobId()));
	}

	private static String selectNonAffordableExecutor(final Map<String, JobWithExecutionEstimation> executors,
			final Instant optimisticDeadline) {
		return executors.entrySet().stream()
				.min(comparing(executor -> optimisticDeadline.isBefore(getCurrentTime()) ?
						computeFinishTime(executor.getValue()).toEpochMilli() :
						executor.getValue().getEstimatedPrice()))
				.map(Map.Entry::getKey)
				.orElse(EMPTY);

	}

	private static String selectAffordableExecutor(final double priority,
			final Map<String, JobWithExecutionEstimation> affordableExecutors,
			final Instant optimisticDeadline, final Long optimisticSpareDeadline, final Double optimisticSpareBudget) {
		return affordableExecutors.entrySet().stream()
				.filter(executor -> computeFinishTime(executor.getValue()).isBefore(optimisticDeadline))
				.min(comparingDouble(executor -> computeBiFactor(priority, executor.getValue(),
						optimisticSpareDeadline, optimisticSpareBudget)))
				.map(Map.Entry::getKey)
				.orElseGet(() -> selectExecutorWithEarliestTime(affordableExecutors));
	}

	private static String selectExecutorWithEarliestTime(
			final Map<String, JobWithExecutionEstimation> affordableExecutors) {
		return affordableExecutors.entrySet().stream()
				.min(comparing(executor -> computeFinishTime(executor.getValue())))
				.map(Map.Entry::getKey)
				.orElseThrow();
	}

}
