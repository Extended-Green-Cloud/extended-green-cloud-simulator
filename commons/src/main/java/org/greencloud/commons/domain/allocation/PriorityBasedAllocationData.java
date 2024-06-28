package org.greencloud.commons.domain.allocation;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing data needed by the priority-based allocation algorithm
 */
@JsonSerialize(as = ImmutablePriorityBasedAllocationData.class)
@JsonDeserialize(as = ImmutablePriorityBasedAllocationData.class)
@Value.Immutable
public interface PriorityBasedAllocationData extends AllocationData {

	List<ClientJob> getJobsToAllocate();
	List<Map<String, Object>> getExecutorsResources();
}
