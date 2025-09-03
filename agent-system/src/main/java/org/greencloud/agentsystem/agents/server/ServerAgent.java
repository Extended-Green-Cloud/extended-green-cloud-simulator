package org.greencloud.agentsystem.agents.server;

import static java.lang.Boolean.FALSE;
import static org.greencloud.commons.constants.DFServiceConstants.SA_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.SA_SERVICE_TYPE;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.server.ServerNode;

/**
 * Agent representing the Server which executes the clients' jobs.
 */
public class ServerAgent extends EGCSAgent<ServerNode, ServerAgentProps, ServerArgs> {

	private static final LogBuilder log = constructLogBuilder(ServerAgent.class);

	public ServerAgent() {
		super();
	}

	@Override
	protected void initializeAgent(final Object[] args) {
		try {
			final EGCSAgentControllerArgs<ServerArgs> controllerArgs = parseAgentArguments(args);
			this.properties = new ServerAgentProps(getName(), controllerArgs.getAgentArgs());

			completeAgentRegistration(controllerArgs);
		} catch (final Exception e) {
			log.messageError("Incorrect arguments: some parameters for Server Agent are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), SA_SERVICE_TYPE, SA_SERVICE_NAME,
				properties.getOwnerRegionalManagerAgent().getName());
		super.takeDown();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		final String ownerName = properties.getOwnerRegionalManagerAgent().getName();
		register(this, getDefaultDF(), SA_SERVICE_TYPE, SA_SERVICE_NAME, ownerName);
	}

	private void completeAgentRegistration(final EGCSAgentControllerArgs<ServerArgs> controllerArgs) {
		if (FALSE.equals(controllerArgs.isInformer())) {
			final String ownerName = properties.getOwnerRegionalManagerAgent().getName();
			register(this, getDefaultDF(), SA_SERVICE_TYPE, SA_SERVICE_NAME, ownerName);
		}
	}
}
