package com.gui.agents.monitoring;

import static org.greencloud.commons.args.agent.AgentType.MONITORING;

import java.io.Serializable;

import org.greencloud.commons.args.agent.monitoring.agent.MonitoringAgentProps;
import org.greencloud.commons.args.agent.monitoring.node.MonitoringNodeArgs;

import com.gui.agents.egcs.EGCSNode;

/**
 * Agent node class representing the monitoring agent
 */
public class MonitoringNode extends EGCSNode<MonitoringNodeArgs, MonitoringAgentProps> implements Serializable {

	/**
	 * Monitoring node constructor
	 *
	 * @param nodeArgs arguments of monitoring agent node
	 */
	public MonitoringNode(final MonitoringNodeArgs nodeArgs) {
		super(nodeArgs, MONITORING);
	}


	@Override
	public void updateGUI(final MonitoringAgentProps props) {
		// GUI is not being modified
	}

	@Override
	public void saveMonitoringData(final MonitoringAgentProps props) {
		// no data is reported
	}
}
