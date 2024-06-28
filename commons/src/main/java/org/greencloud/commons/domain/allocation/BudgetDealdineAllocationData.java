package org.greencloud.commons.domain.allocation;

import java.util.Map;

import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing data needed by the budget-deadline oriented allocation algorithm
 */
@JsonSerialize(as = ImmutableBudgetDealdineAllocationData.class)
@JsonDeserialize(as = ImmutableBudgetDealdineAllocationData.class)
@Value.Immutable
public interface BudgetDealdineAllocationData extends AllocationData {

	ClientJob getJobForAllocation();

	Double getJobPriority();

	Map<String, JobWithExecutionEstimation> getExecutorsEstimations();

	Map<String, ServerResources> getExecutorsResources();
}
