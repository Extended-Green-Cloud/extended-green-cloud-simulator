package com.gui.controller;

import com.gui.agents.AbstractAgentNode;
import com.gui.event.domain.PowerShortageEvent;

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
	 * Method triggers the power shortage event in the Cloud Network for specified agent
	 *
	 * @param powerShortageEvent data for event triggering
	 * @param agentName          name of the agent for which the event is being triggered
	 */
	void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName);

}
