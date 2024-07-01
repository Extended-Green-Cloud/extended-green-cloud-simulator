package org.greencloud.agentsystem.strategies.algorithms.allocation;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.utils.datastructures.MapConstructor.constructAtomicMap;
import static org.greencloud.commons.utils.datastructures.MapConstructor.constructListMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.greencloud.commons.domain.job.basic.ClientJob;

/**
 * Class with method performing LC allocation
 */
public class LeastConnectionAllocator {

	/**
	 * Method performs an allocation based on the least connections number.
	 *
	 * @param jobsToAllocate jobs that are to be allocated
	 * @param connections    connections per each executor
	 * @return mapping between executors identifiers and resource identifiers
	 */
	public static Map<String, List<String>> leastConnectionAllocation(
			final List<ClientJob> jobsToAllocate,
			final Map<String, Long> connections) {
		final Map<String, List<String>> executorsPerJob = constructListMap(connections.keySet());
		final Map<String, AtomicReference<Long>> connectionsCopy = constructAtomicMap(connections);

		jobsToAllocate.stream()
				.map(ClientJob::getJobId)
				.forEach(job -> {
					final Optional<String> selectedExecutor = connectionsCopy.entrySet().stream()
							.min(comparingLong(entry -> entry.getValue().get()))
							.map(Map.Entry::getKey);

					selectedExecutor.ifPresent(executor -> {
						executorsPerJob.get(executor).add(job);
						connectionsCopy.get(executor).updateAndGet(val -> val + 1);
					});
				});

		return executorsPerJob;
	}
}
