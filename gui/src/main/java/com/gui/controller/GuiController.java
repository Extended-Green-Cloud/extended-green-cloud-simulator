package com.gui.controller;

import java.io.Serializable;
import java.time.Instant;

import com.gui.agents.AbstractNode;
import com.gui.event.PowerShortageEvent;

/**
 * Controller for GUI
 */
public interface GuiController extends Runnable, Serializable {

	/**
	 * Method creates the GUI
	 */
	void run();

	/**
	 * Method adds next agent node to the graph
	 *
	 * @param agent node of the specified agent
	 */
	void addAgentNodeToGraph(final AbstractNode agent);

	/**
	 * Method reports to the socket server time when the system was started
	 *
	 * @param time time of system start
	 */
	void reportSystemStartTime(final Instant time);

	/**
	 * Method triggers the power shortage event in the Cloud Network for specified agent
	 *
	 * @param powerShortageEvent data for event triggering
	 * @param agentName          name of the agent for which the event is being triggered
	 */
	void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName);

}
