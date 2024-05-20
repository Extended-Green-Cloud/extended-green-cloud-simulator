package com.greencloud.connector.gui;

import java.io.Serializable;
import java.time.Instant;

import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.domain.EventData;
import org.jrba.environment.domain.ExternalEvent;

import com.greencloud.connector.factory.EGCSControllerFactory;

/**
 * Controller for GUI.
 */
@SuppressWarnings("rawtypes")
public interface GuiController extends Runnable, Serializable {

	/**
	 * Method creates the GUI.
	 */
	void run();

	/**
	 * Method connects GUI with agent factory.
	 */
	void connectWithAgentFactory(final EGCSControllerFactory factory);

	/**
	 * Method adds next agent node to the graph.
	 *
	 * @param agent node of the specified agent
	 */
	void addAgentNodeToGraph(final EGCSNode agent);

	/**
	 * Method reports to the socket server time when the system was started.
	 *
	 * @param time time of system start
	 */
	void reportSystemStartTime(final Instant time);

	/**
	 * Method triggers agent event.
	 *
	 * @param event data for the event
	 */
	void triggerEvent(final ExternalEvent event);

	/**
	 * Method triggers agent event.
	 *
	 * @param eventData data for the event
	 */
	void triggerEvent(final EventData eventData);

}
