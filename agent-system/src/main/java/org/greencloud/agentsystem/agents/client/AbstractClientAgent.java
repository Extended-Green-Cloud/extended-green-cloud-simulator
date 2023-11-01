package org.greencloud.agentsystem.agents.client;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;

/**
 * Abstract agent class representing Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent<ClientNode, ClientAgentProps> {

	protected AbstractClientAgent() {
		super();
		this.properties = new ClientAgentProps(getName());
	}

}
