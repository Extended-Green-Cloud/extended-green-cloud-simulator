package org.greencloud.agentsystem.agents.greenenergy;

import static java.lang.Boolean.FALSE;
import static org.greencloud.commons.constants.DFServiceConstants.GS_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.GS_SERVICE_TYPE;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;

/**
 * Agent representing the Green Energy Source that produces the power for the Servers.
 */
public class GreenEnergyAgent extends EGCSAgent<GreenEnergyNode, GreenEnergyAgentProps, GreenEnergyArgs> {

	private static final LogBuilder log = constructLogBuilder(GreenEnergyAgent.class);

	public GreenEnergyAgent() {
		super();
	}

	@Override
	protected void initializeAgent(final Object[] args) {
		try {
			final EGCSAgentControllerArgs<GreenEnergyArgs> controllerArgs = parseAgentArguments(args);
			this.properties = new GreenEnergyAgentProps(getName(), controllerArgs.getAgentArgs());

			completeAgentRegistration(controllerArgs);
		} catch (final Exception e) {
			log.messageError("Incorrect arguments: some parameters for Green Source Agent are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, properties.getOwnerServerAgent().getName());
		super.takeDown();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		register(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, properties.getOwnerServerAgent().getName());
	}

	private void completeAgentRegistration(final EGCSAgentControllerArgs<GreenEnergyArgs> controllerArgs) {
		if (FALSE.equals(controllerArgs.isInformer())) {
			final String ownerName = properties.getOwnerServerAgent().getName();
			register(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerName);
		}
	}
}
