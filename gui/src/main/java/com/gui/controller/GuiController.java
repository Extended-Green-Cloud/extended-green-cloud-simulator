package com.gui.controller;

import java.io.Serializable;
import java.time.Instant;

import com.gui.agents.egcs.EGCSNode;
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
	void addAgentNodeToGraph(final EGCSNode agent);

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
	 */
	void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent);

}
