package org.greencloud.agentsystem.agents.greenenergy;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;

/**
 * Abstract agent class storing data of the Green Source Energy Agent
 */
public abstract class AbstractGreenEnergyAgent extends AbstractAgent<GreenEnergyNode, GreenEnergyAgentProps> {

	AbstractGreenEnergyAgent() {
		super();
		this.properties = new GreenEnergyAgentProps(getName());
	}

}
