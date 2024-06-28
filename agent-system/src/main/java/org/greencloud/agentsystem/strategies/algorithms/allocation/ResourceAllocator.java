package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static org.greencloud.agentsystem.strategies.algorithms.allocation.BudgetDeadlineAllocator.budgetDeadlineConstrainedAllocation;
import static org.greencloud.agentsystem.strategies.algorithms.allocation.IntentBasedAllocator.intentBasedResourceAllocation;
import static org.greencloud.agentsystem.strategies.algorithms.allocation.LeastConnectionAllocator.leastConnectionAllocation;
import static org.greencloud.agentsystem.strategies.algorithms.allocation.PriorityBasedAllocator.priorityBasedKMeansAllocation;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.allocation.AllocationData;
import org.greencloud.commons.domain.allocation.BudgetDealdineAllocationData;
import org.greencloud.commons.domain.allocation.IntentBasedAllocationData;
import org.greencloud.commons.domain.allocation.LeastConnectionAllocationData;
import org.greencloud.commons.domain.allocation.PriorityBasedAllocationData;
import org.greencloud.commons.exception.AllocationAlgorithmNotFoundException;
import org.jrba.agentmodel.domain.props.AgentProps;

/**
 * Class implementing methods used to allocate tasks to resources
 */
public class ResourceAllocator {

	/**
	 * Method performs allocation of the jobs to the executors.
	 *
	 * @param allocationData data used by the allocation algorithm
	 * @param agentProps     agent properties
	 * @param <T>            type of allocation data
	 * @param <E>            type of agent properties
	 * @return map with executor identifier and assigned jobs identifiers
	 */
	public static <T extends AllocationData, E extends AgentProps> Map<String, List<String>> allocate(
			final T allocationData, final E agentProps) {
		return switch (allocationData) {
			case IntentBasedAllocationData intentBasedAllocationData ->
					intentBasedAllocation(intentBasedAllocationData, agentProps);
			case PriorityBasedAllocationData priorityBasedAllocationData ->
					priorityBasedAllocation(priorityBasedAllocationData, agentProps);
			case BudgetDealdineAllocationData budgetDeadlineAllocationData ->
					budgetDeadlineAllocation(budgetDeadlineAllocationData);
			case LeastConnectionAllocationData leastConnectionAllocationData ->
					leastConnectionAllocator(leastConnectionAllocationData);
			default -> throw new AllocationAlgorithmNotFoundException();
		};
	}

	private static Map<String, List<String>> budgetDeadlineAllocation(
			final BudgetDealdineAllocationData allocationData) {
		return budgetDeadlineConstrainedAllocation(
				allocationData.getJobForAllocation(),
				allocationData.getJobPriority(),
				allocationData.getModifications(),
				allocationData.getExecutorsResources(),
				allocationData.getExecutorsEstimations()
		);
	}

	private static <E extends AgentProps> Map<String, List<String>> priorityBasedAllocation(
			final PriorityBasedAllocationData allocationData, final E agentProps) {
		return priorityBasedKMeansAllocation(
				allocationData.getJobsToAllocate(),
				allocationData.getExecutorsResources(),
				agentProps,
				allocationData.getModifications()
		);
	}

	private static <E extends AgentProps> Map<String, List<String>> intentBasedAllocation(
			final IntentBasedAllocationData allocationData, final E agentProps) {
		return intentBasedResourceAllocation(
				allocationData.getModifications(),
				allocationData.getJobResources(),
				allocationData.getExecutorsResources(),
				agentProps,
				allocationData.getClusterNoJobs(),
				allocationData.getClusterNoExecutors()
		);
	}

	private static Map<String, List<String>> leastConnectionAllocator(
			final LeastConnectionAllocationData allocationData) {
		return leastConnectionAllocation(
				allocationData.getJobsToAllocate(),
				allocationData.getRMAConnections()
		);
	}
}
