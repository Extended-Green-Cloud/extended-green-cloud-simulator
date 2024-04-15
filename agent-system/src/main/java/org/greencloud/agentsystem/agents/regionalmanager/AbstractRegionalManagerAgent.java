package org.greencloud.agentsystem.agents.regionalmanager;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;

/**
 * Abstract agent class storing the data regarding Regional Manager Agent
 */
public abstract class AbstractRegionalManagerAgent extends EGCSAgent<RegionalManagerNode, RegionalManagerAgentProps> {

	AbstractRegionalManagerAgent() {
		super();
		this.properties = new RegionalManagerAgentProps(getName());
	}
}
