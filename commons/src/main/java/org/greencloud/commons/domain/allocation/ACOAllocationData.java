package org.greencloud.commons.domain.allocation;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing data needed by the ACO-based allocation algorithm
 */
@JsonSerialize(as = ImmutableACOAllocationData.class)
@JsonDeserialize(as = ImmutableACOAllocationData.class)
@Value.Immutable
public interface ACOAllocationData extends AllocationData {

	AntAllocationParameters getAntParams();
	List<ClientJob> getJobsToAllocate();

	List<Map<String, Object>> getExecutorsResources();
}
