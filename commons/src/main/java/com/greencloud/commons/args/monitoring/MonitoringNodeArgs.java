package com.greencloud.commons.args.monitoring;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.AgentArgs;

@JsonDeserialize(as = ImmutableMonitoringNodeArgs.class)
@JsonSerialize(as = ImmutableMonitoringNodeArgs.class)
@Value.Immutable
public interface MonitoringNodeArgs extends AgentArgs {

	String getGreenEnergyAgent();
}
