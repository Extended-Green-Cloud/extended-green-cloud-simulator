package org.greencloud.commons.utils.allocation;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;

/**
 * Class with methods used to compute allocation-specific quality metrics
 */
public class AllocationMetrics {

	/**
	 * Method computes the ratio between jobs that were accepted by the executors to the total number of allocated jobs.
	 *
	 * @param allocatedJobs allocation response
	 * @return AAR
	 */
	public static double computeAllocationAcceptanceRatio(final AllocatedJobs allocatedJobs) {
		final long acceptedAllocatedJobsNumber = allocatedJobs.getAllocationJobs().size();
		final long totalJobsNumber = (long) allocatedJobs.getAllocationJobs().size() +
				ofNullable(allocatedJobs.getRejectedAllocationJobs()).orElse(emptyList()).size();

		return (double) acceptedAllocatedJobsNumber / totalJobsNumber;
	}

	/**
	 * Method computes the ratio between jobs for which the executors were allocated to the total number of jobs
	 * planned for allocation.
	 *
	 * @param jobs          jobs planned for allocation
	 * @param allocatedJobs allocation result
	 * @return ASR
	 */
	public static double computeAllocationSuccessRatio(final List<ClientJob> jobs,
			final Map<String, List<String>> allocatedJobs) {
		final long allocatedJobsNumber = allocatedJobs.values().stream().mapToLong(Collection::size).sum();
		final long totalJobsNumber = jobs.size();

		return  (double) allocatedJobsNumber / totalJobsNumber;
	}
}
