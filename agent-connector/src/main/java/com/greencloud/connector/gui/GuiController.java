package com.greencloud.connector.gui;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

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
	 * Method reports to the socket server the parameters of strategy allocation strategy.
	 *
	 * @param resourceAllocationStrategy name of the resource allocation algorithm
	 * @param prioritizationStrategy     name of the tasks' prioritization algorithm
	 * @param appliedModifications       list of applied modifications (possibly empty when no modifications were applied)
	 * @param numberOfAllocationSteps    number of steps of resource allocation algorithm
	 */
	void reportStrategyParameters(final String resourceAllocationStrategy,
			final String prioritizationStrategy,
			final List<String> appliedModifications,
			final int numberOfAllocationSteps);

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
