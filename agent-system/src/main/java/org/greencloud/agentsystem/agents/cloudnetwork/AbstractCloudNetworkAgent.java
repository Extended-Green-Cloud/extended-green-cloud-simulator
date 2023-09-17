package org.greencloud.agentsystem.agents.cloudnetwork;

import org.greencloud.agentsystem.agents.AbstractAgent;

import com.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import com.gui.agents.CloudNetworkNode;

/**
 * Abstract agent class storing the data regarding Cloud Network Agent
 */
public abstract class AbstractCloudNetworkAgent extends AbstractAgent<CloudNetworkNode, CloudNetworkAgentProps> {

	AbstractCloudNetworkAgent() {
		super();
		this.properties = new CloudNetworkAgentProps(getName());
	}
}
