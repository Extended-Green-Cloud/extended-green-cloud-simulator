package org.greencloud.commons.domain.allocation;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface that needs to be extended by objects representing data needed by the allocation algorithms
 */
@JsonSerialize(as = ImmutableAllocationData.class)
@JsonDeserialize(as = ImmutableAllocationData.class)
@Value.Immutable
public interface AllocationData {

	@Nullable
	Map<String, List<String>> getServersPerRMA();
}
