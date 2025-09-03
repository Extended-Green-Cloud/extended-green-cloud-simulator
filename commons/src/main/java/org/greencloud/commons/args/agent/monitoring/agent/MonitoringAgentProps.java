package org.greencloud.commons.args.agent.monitoring.agent;

import static org.greencloud.commons.args.agent.EGCSAgentType.MONITORING;

import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;

import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Monitoring Agent
 */
@Getter
@Setter
public class MonitoringAgentProps extends EGCSAgentProps<MonitoringArgs> {

	/**
	 * Constructor that initialize Monitoring Agent properties to initial values
	 *
	 * @param agentName name of the agent
	 */
	public MonitoringAgentProps(final String agentName, final MonitoringArgs monitoringArgs) {
		super(MONITORING, agentName, monitoringArgs);
	}
}
