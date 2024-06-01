package org.greencloud.commons.domain.allocation;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing data needed by the intent-based allocation algorithms
 */
@JsonSerialize(as = ImmutableIntentBasedAllocationData.class)
@JsonDeserialize(as = ImmutableIntentBasedAllocationData.class)
@Value.Immutable
public interface IntentBasedAllocationData extends AllocationData {

	List<Map<String, Object>> getJobResources();
	List<Map<String, Object>> getExecutorsResources();
	Integer getClusterNoJobs();
	Integer getClusterNoExecutors();
}
