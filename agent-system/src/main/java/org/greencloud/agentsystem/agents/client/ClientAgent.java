package org.greencloud.agentsystem.agents.client;

import static java.util.Collections.emptyList;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.jrba.utils.agent.AgentConnector.connectAgentObject;
import static org.jrba.utils.yellowpages.YellowPagesRegister.prepareDF;

import java.util.List;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.client.ClientNode;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Agent representing the Client that wants to have the job executed in the Cloud.
 */
public class ClientAgent extends EGCSAgent<ClientNode, ClientAgentProps, ClientArgs> {

	private static final LogBuilder log = constructLogBuilder(ClientAgent.class);

	public ClientAgent() {
		super();
	}

	@Override
	public void initializeAgent(final Object[] args) {
		try {
			final EGCSAgentControllerArgs<ClientArgs> controllerArgs = parseAgentArguments(args);
			this.properties = new ClientAgentProps(getName(), getAID(), controllerArgs.getAgentArgs());

			properties.setParentDFAddress(prepareDF(controllerArgs.getMainDFAddress().orElseThrow(),
					controllerArgs.getMainHostPlatformId().orElseThrow()));
		} catch (final Exception e) {
			log.messageError("Incorrect arguments: some parameters for client's job are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		connectClient(this);

		final ParallelBehaviour main = new ParallelBehaviour();
		addBehaviour(main);
		setMainBehaviour(main);
		return emptyList();
	}

	@Override
	protected void setup() {
		super.setup();
		logClientSetUp();
	}

	private void connectClient(EGCSAgent<?, ?, ?> abstractAgent) {
		connectAgentObject(abstractAgent, abstractAgent.getO2AObject());
		connectAgentObject(abstractAgent, abstractAgent.getO2AObject());
		runInitialBehavioursForRuleSet();
	}

	private void logClientSetUp() {
		log.withJobId(properties.getJob().getJobId())
				.messageInfo("[{}] Job deadline: {}. Job type: {}", getName(), properties.getJobSimulatedDeadline(),
						properties.getJobType());
	}
}
