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

	/**
	 * @return importance of the job deadline taken into account in its scheduling
	 */
	Double getDeadlinePriority();

	/**
	 * @return importance of the job cpu requirement taken into account in its scheduling
	 */
	Double getCpuPriority();

	/**
	 * @return maximal size of scheduler job queue
	 */
	Integer getMaxQueueSize();
}
