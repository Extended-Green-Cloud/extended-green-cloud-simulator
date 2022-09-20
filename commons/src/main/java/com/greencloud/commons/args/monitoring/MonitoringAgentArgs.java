package com.greencloud.commons.args.monitoring;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.AgentArgs;

/**
 * Arguments of the Monitoring Agent
 */
@JsonSerialize(as = ImmutableMonitoringAgentArgs.class)
@JsonDeserialize(as = ImmutableMonitoringAgentArgs.class)
@Value.Immutable
public interface MonitoringAgentArgs extends AgentArgs {
}
