package com.greencloud.application.agents.client;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.CLIENT_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.CLIENT;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.client.domain.ClientJobExecution;
import com.greencloud.application.agents.client.management.ClientManagement;

/**
 * Abstract agent class storing the data regarding Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent {

	protected ClientJobExecution jobExecution;
	protected boolean announced;

	protected AbstractClientAgent() {
		super();
		this.agentType = CLIENT;
	}

	public ClientManagement manage() {
		return (ClientManagement) agentManagementServices.get(CLIENT_MANAGEMENT);
	}

	public ClientJobExecution getJobExecution() {
		return jobExecution;
	}

	public void announce() {
		announced = true;
	}

	public boolean isAnnounced() {
		return announced;
	}

}
