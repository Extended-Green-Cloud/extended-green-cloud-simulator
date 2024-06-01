package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static org.greencloud.agentsystem.strategies.algorithms.allocation.IntentBasedAllocator.intentBasedResourceAllocation;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.allocation.AllocationData;
import org.greencloud.commons.domain.allocation.IntentBasedAllocationData;
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
			default -> throw new AllocationAlgorithmNotFoundException();
		};
	}

	private static <E extends AgentProps> Map<String, List<String>> intentBasedAllocation(
			final IntentBasedAllocationData allocationData, final E agentProps) {
		return intentBasedResourceAllocation(
				allocationData.getJobResources(),
				allocationData.getExecutorsResources(),
				agentProps,
				allocationData.getClusterNoJobs(),
				allocationData.getClusterNoExecutors()
		);
	}
}
