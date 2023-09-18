package com.gui.agents;

import java.io.Serializable;

public interface AbstractNodeInterface extends Serializable {

	/**
	 * Method retrieves the agent name
	 *
	 * @return agent name
	 */
	String getAgentName();

	/**
	 * Method responsible for adding the node to the graph
	 */
	void addToGraph();

	/**
	 * Method remove agent node from the graph
	 */
	void removeAgentNodeFromGraph();
}
