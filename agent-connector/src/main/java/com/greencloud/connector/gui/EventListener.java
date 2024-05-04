package com.greencloud.connector.gui;

import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.domain.EventData;
import org.jrba.environment.domain.ExternalEvent;

/**
 * Service responsible for handling external events.
 */
@SuppressWarnings("rawtypes")
public interface EventListener {

	/**
	 * Method used to connect new agent node with a listener
	 *
	 * @param agentNode node that is to be taken into account in event listener
	 */
	void addAgentNode(final EGCSNode agentNode);

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
