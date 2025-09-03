package org.greencloud.managingsystem.service;

import org.greencloud.managingsystem.agents.ManagingAgent;

/**
 * Abstract service inherited by all services used in managing agent
 */
public abstract class AbstractManagingService {

	protected ManagingAgent managingAgent;

	/**
	 * Default constructor
	 *
	 * @param managingAgent agent using the service to monitor the system
	 */
	protected AbstractManagingService(final ManagingAgent managingAgent) {
		this.managingAgent = (ManagingAgent) managingAgent;
	}
}
