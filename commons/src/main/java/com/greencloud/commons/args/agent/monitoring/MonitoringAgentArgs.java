package com.greencloud.commons.args.agent.monitoring;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

/**
 * Arguments of the Monitoring Agent
 */
@JsonSerialize(as = ImmutableMonitoringAgentArgs.class)
@JsonDeserialize(as = ImmutableMonitoringAgentArgs.class)
@Value.Immutable
public interface MonitoringAgentArgs extends AgentArgs {

	/**
	 * @return optional bad stub probability
	 */
	@Nullable
	Double getBadStubProbability();

}
