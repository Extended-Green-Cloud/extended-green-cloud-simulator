package org.greencloud.commons.domain.allocation;

import java.util.List;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing jobs that need to be allocated
 */
@JsonSerialize(as = ImmutableAllocatedJobs.class)
@JsonDeserialize(as = ImmutableAllocatedJobs.class)
@Value.Immutable
public interface AllocatedJobs {

	List<ClientJobWithServer> getAllocationJobs();

	@Nullable
	List<ClientJobWithServer> getRejectedAllocationJobs();
}
