package org.greencloud.commons.args.agent.monitoring.node;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.agent.AgentArgs;

/**
 * Arguments used to construct GUI node of Monitoring Agent
 */
@JsonDeserialize(as = ImmutableMonitoringNodeArgs.class)
@JsonSerialize(as = ImmutableMonitoringNodeArgs.class)
@Value.Immutable
public interface MonitoringNodeArgs extends AgentArgs {

	String getGreenEnergyAgent();
}
