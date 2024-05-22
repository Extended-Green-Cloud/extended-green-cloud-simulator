package org.greencloud.agentsystem.agents.server;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;

/**
 * Abstract agent class storing data of the Server Agent.
 */
public abstract class AbstractServerAgent extends EGCSAgent<ServerNode, ServerAgentProps> {

	AbstractServerAgent() {
		super();
		this.properties = new ServerAgentProps(getName());
	}
}
