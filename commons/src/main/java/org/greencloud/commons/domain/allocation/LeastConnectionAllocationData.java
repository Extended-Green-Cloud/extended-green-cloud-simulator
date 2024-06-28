package org.greencloud.commons.domain.allocation;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing data needed by the least connection allocation algorithm
 */
@JsonSerialize(as = ImmutableLeastConnectionAllocationData.class)
@JsonDeserialize(as = ImmutableLeastConnectionAllocationData.class)
@Value.Immutable
public interface LeastConnectionAllocationData extends AllocationData {

	List<ClientJob> getJobsToAllocate();

	Map<String, Long> getRMAConnections();
}
