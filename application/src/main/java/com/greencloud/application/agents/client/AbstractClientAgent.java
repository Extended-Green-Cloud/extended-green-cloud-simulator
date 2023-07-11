package com.greencloud.application.agents.client;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.CLIENT_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.CLIENT;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.client.domain.ClientJobExecution;
import com.greencloud.application.agents.client.management.ClientManagement;

/**
 * Abstract agent class storing the data regarding Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent {

	protected final ConcurrentMap<String, ClientJobExecution> jobParts;
	protected ClientJobExecution jobExecution;
	protected boolean announced;
	protected boolean split;
	protected boolean isInCloud;

	protected AbstractClientAgent() {
		super();
		this.jobParts = new ConcurrentHashMap<>();
		this.agentType = CLIENT;
		this.isInCloud = false;
	}

	public ClientManagement manage() {
		return (ClientManagement) agentManagementServices.get(CLIENT_MANAGEMENT);
	}

	public ConcurrentMap<String, ClientJobExecution> getJobParts() {
		return jobParts;
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

	public boolean isSplit() {
		return split;
	}

	public void split() {
		split = true;
	}

	public boolean isInCloud() {
		return isInCloud;
	}

	public void setInCloud(boolean inCloud) {
		isInCloud = inCloud;
	}
}
