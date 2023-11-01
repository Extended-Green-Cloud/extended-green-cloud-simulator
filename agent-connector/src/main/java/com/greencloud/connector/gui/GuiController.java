package com.greencloud.connector.gui;

import java.io.Serializable;
import java.time.Instant;

import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.PowerShortageEvent;

import com.greencloud.connector.factory.AgentControllerFactory;

/**
 * Controller for GUI
 */
public interface GuiController extends Runnable, Serializable {

	/**
	 * Method creates the GUI
	 */
	void run();

	/**
	 * Method connects GUI with agent factory
	 */
	void connectWithAgentFactory(final AgentControllerFactory factory);

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
