package com.greencloud.commons.args.agent.scheduler;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

@Value.Immutable
@JsonSerialize(as = ImmutableSchedulerNodeArgs.class)
@JsonDeserialize(as = ImmutableSchedulerNodeArgs.class)
public interface SchedulerNodeArgs extends AgentArgs {

	Double getDeadlinePriority();
	Double getPowerPriority();
	Integer getMaxQueueSize();
}
