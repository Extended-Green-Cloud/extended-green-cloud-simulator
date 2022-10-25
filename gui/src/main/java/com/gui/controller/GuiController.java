package com.gui.controller;

import java.util.List;

import com.gui.agents.AbstractAgentNode;

/**
 * Controller for GUI
 */
public interface GuiController extends Runnable {

	/**
	 * Method creates the GUI
	 */
	void run();

	/**
	 * Method adds next agent node to the graph
	 *
	 * @param agent node of the specified agent
	 */
	void addAgentNodeToGraph(final AbstractAgentNode agent);

	/**
	 * Method remove agent node from the graph
	 *
	 * @param agentNode agent node that is to be removed
	 */
	void removeAgentNodeFromGraph(final AbstractAgentNode agentNode);

	/**
	 * Method updates the clients number by given value
	 *
	 * @param value value to be added to the clients number
	 */
	void updateClientsCountByValue(final int value);

	/**
	 * Method updates the active jobs number by given value
	 *
	 * @param value value to be added to the active jobs number
	 */
	void updateActiveJobsCountByValue(final int value);

	/**
	 * Method updates the all jobs number by given value
	 *
	 * @param value value to be added to the all jobs number
	 */
	void updateAllJobsCountByValue(final int value);

	/**
	 * Method draws an arrow to show the messages flow between com.greencloud.application.agents
	 *
	 * @param senderAgent    agent sending the message
	 * @param receiversNames names of the com.greencloud.application.agents receiving the message
	 */
	void displayMessageArrow(final AbstractAgentNode senderAgent, final List<String> receiversNames);

}
