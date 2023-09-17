package org.greencloud.agentsystem.agents.server;

import org.greencloud.agentsystem.agents.AbstractAgent;

import com.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import com.gui.agents.ServerNode;

/**
 * Abstract agent class storing data of the Server Agent
 */
public abstract class AbstractServerAgent extends AbstractAgent<ServerNode, ServerAgentProps> {

	AbstractServerAgent() {
		super();
		this.properties = new ServerAgentProps(getName());
	}
}
