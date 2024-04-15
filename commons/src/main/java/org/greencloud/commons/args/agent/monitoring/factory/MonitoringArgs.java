package org.greencloud.commons.args.agent.monitoring.factory;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jrba.agentmodel.domain.args.AgentArgs;

/**
 * Arguments used to build Monitoring Agent
 */
@JsonSerialize(as = ImmutableMonitoringArgs.class)
@JsonDeserialize(as = ImmutableMonitoringArgs.class)
@Value.Immutable
public interface MonitoringArgs extends AgentArgs {

	/**
	 * @return optional bad stub probability
	 */
	@Nullable
	Double getBadStubProbability();

}
