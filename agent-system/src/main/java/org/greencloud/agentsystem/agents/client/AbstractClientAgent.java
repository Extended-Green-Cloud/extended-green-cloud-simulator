package org.greencloud.agentsystem.agents.client;

import org.greencloud.agentsystem.agents.AbstractAgent;

import com.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import com.gui.agents.ClientNode;

/**
 * Abstract agent class representing Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent<ClientNode, ClientAgentProps> {

	protected AbstractClientAgent() {
		super();
		this.properties = new ClientAgentProps(getName());
	}

}
