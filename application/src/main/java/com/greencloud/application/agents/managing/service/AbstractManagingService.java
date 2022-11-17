package com.greencloud.application.agents.managing.service;

import com.greencloud.application.agents.managing.AbstractManagingAgent;
import com.greencloud.application.agents.managing.ManagingAgent;

/**
 * Abstract service inherited by all services implementing the MAPEK reference model
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
