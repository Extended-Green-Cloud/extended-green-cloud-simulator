package com.greencloud.connector.factory;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.agents.monitoring.MonitoringNode;

/**
 * Factory used to create agent nodes
 */
public interface AgentNodeFactory {

	/**
	 * Method creates the graph node based on the scenario arguments
	 *
	 * @param agentArgs    current agent arguments
	 * @param scenarioArgs scenario arguments
	 */
	EGCSNode<?, ?> createAgentNode(final AgentArgs agentArgs, final ScenarioStructureArgs scenarioArgs);

	/**
	 * Method creates monitoring agent node
	 *
	 * @param monitoringArgs  monitoring agent arguments
	 * @param greenSourceName name of the owner green source
	 * @return monitoring agent node
	 */
	MonitoringNode createMonitoringNode(final MonitoringArgs monitoringArgs, final String greenSourceName);
}
