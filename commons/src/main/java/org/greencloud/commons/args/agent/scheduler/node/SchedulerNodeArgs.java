package org.greencloud.commons.args.agent.scheduler.node;

import org.greencloud.commons.args.agent.AgentArgs;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to construct GUI node of Scheduler Agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSchedulerNodeArgs.class)
@JsonDeserialize(as = ImmutableSchedulerNodeArgs.class)
public interface SchedulerNodeArgs extends AgentArgs {

	Double getDeadlinePriority();
	Double getCpuPriority();
	Integer getMaxQueueSize();
}
