package org.greencloud.managingsystem.service;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.agent.ManagingAgent;

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
	protected AbstractManagingService(AbstractManagingAgent managingAgent) {
		this.managingAgent = (ManagingAgent) managingAgent;
	}
}
