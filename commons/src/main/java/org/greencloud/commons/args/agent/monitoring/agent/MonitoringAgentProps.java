package org.greencloud.commons.args.agent.monitoring.agent;

import static org.greencloud.commons.args.agent.EGCSAgentType.MONITORING;

import org.greencloud.commons.args.agent.EGCSAgentProps;

import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Monitoring Agent
 */
@Getter
@Setter
public class MonitoringAgentProps extends EGCSAgentProps {

	public static final double BAD_STUB_PROBABILITY = 0.02;

	protected double badStubProbability;
	protected boolean offlineMode;

	/**
	 * Constructor that initialize Monitoring Agent properties to initial values
	 *
	 * @param agentName name of the agent
	 */
	public MonitoringAgentProps(final String agentName) {
		super(MONITORING, agentName);
		this.badStubProbability = BAD_STUB_PROBABILITY;
		this.offlineMode = false;
	}
}
