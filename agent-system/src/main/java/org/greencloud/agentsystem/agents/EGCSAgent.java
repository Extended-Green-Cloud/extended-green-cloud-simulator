package org.greencloud.agentsystem.agents;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.greencloud.commons.args.agent.EGCSAgentType.isClient;
import static org.greencloud.commons.args.agent.EGCSAgentType.isCloudAgent;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareMessageToManagingAgent;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_CLIENT_NAME;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.greencloud.agentsystem.behaviours.ListenForAdaptationAction;
import org.greencloud.agentsystem.behaviours.ReportHealthCheck;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.exception.InvalidPropertiesException;
import org.greencloud.commons.exception.JadeContainerException;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.jrba.agentmodel.behaviour.ListenForControllerObjects;
import org.jrba.agentmodel.domain.AbstractAgent;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.rulesengine.RulesController;
import org.slf4j.MDC;

import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.wrapper.ControllerException;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class representing agent which has the connection with GUI controller.
 */
@Getter
@Setter
@SuppressWarnings("unchecked")
public abstract class EGCSAgent<T extends EGCSNode<?, E>, E extends EGCSAgentProps<F>, F extends AgentArgs>
		extends AbstractAgent<T, E> {

	private static final LogBuilder log = constructLogBuilder(EGCSAgent.class);
	protected ParallelBehaviour mainBehaviour;

	@Override
	public void setRulesController(final RulesController<E, T> rulesController) {
		this.rulesController = rulesController;
		properties.setAgentName(getName());
		ofNullable(agentNode).ifPresent(node -> {
			properties.setAgentNode(node);
			node.writeAgentConfiguration(getName(),
					Arrays.stream(getAID().getAddressesArray()).findFirst().orElseThrow(),
					properties.getAgentType(),
					properties.getConfig()
			);
		});
		rulesController.setAgent(this, properties, agentNode, getDefaultRuleSet());
	}

	@Override
	protected void runStartingBehaviours() {
		addBehaviour(new ListenForControllerObjects(this, prepareStartingBehaviours(), getObjectsNumber(),
				initializeBehavioursWhenAgentIsConnected()));

	}

	@Override
	public void clean(boolean ok) {
		if (shouldAgentNodeBeRemoved(ok))
			getAgentNode().removeAgentNodeFromGraph();
		super.clean(ok);
	}

	@Override
	protected void setup() {
		log.messageInfo("Setting up Agent {}", getName());
		super.setup();
		setUpLogger();

		if (isCloudAgent.test(properties)) {
			final EGCSAgentControllerArgs<?> controllerArgs = (EGCSAgentControllerArgs<?>) getArguments()[0];
			updateKnowledgeMap(controllerArgs);

			// checking if the managing agent should be informed about agent creation
			if (TRUE.equals(controllerArgs.isInformer()))
				informManagingAboutContainerInitialization(controllerArgs);
		}
	}

	@Override
	protected void takeDown() {
		setUpLogger();
		super.takeDown();
	}

	@Override
	public void addBehaviour(Behaviour b) {
		ofNullable(mainBehaviour)
				.filter(not(behaviour -> behaviour.equals(b)))
				.ifPresentOrElse(behaviour -> behaviour.addSubBehaviour(b), () -> super.addBehaviour(b));
	}

	@Override
	protected int getObjectsNumber() {
		return 2;
	}

	/**
	 * @return database instance
	 */
	public TimescaleDatabase getDatabase() {
		return ofNullable(agentNode)
				.map(EGCSNode::getDatabaseClient)
				.orElse(null);
	}

	private Consumer<List<Behaviour>> initializeBehavioursWhenAgentIsConnected() {
		return initialBehaviours -> {
			final ParallelBehaviour behaviour = new ParallelBehaviour();
			initialBehaviours.forEach(behaviour::addSubBehaviour);
//			behaviour.addSubBehaviour(new ReportHealthCheck(this));
//			behaviour.addSubBehaviour(new ListenForAdaptationAction(this));
			addBehaviour(behaviour);
			setMainBehaviour(behaviour);
			runInitialBehavioursForRuleSet();
		};
	}

	private void setUpLogger() {
		final String loggerName = isClient.test(properties) ? MDC_CLIENT_NAME : MDC_AGENT_NAME;
		MDC.put(loggerName, super.getLocalName());
	}

	protected <F extends AgentArgs> EGCSAgentControllerArgs<F> parseAgentArguments(final Object[] args) {
		return (EGCSAgentControllerArgs<F>) args[0];
	}

	private boolean shouldAgentNodeBeRemoved(final boolean ok) {
		return !ok && nonNull(getAgentNode()) && !isClient.test(properties);
	}

	private void updateKnowledgeMap(final EGCSAgentControllerArgs<?> controllerArgs) {
		controllerArgs.getSystemKnowledge().ifPresent(map -> properties.setSystemKnowledge(map));
	}

	private void informManagingAboutContainerInitialization(final EGCSAgentControllerArgs<?> controllerArgs) {
		try {
			final AID managingAgent = controllerArgs.getManagingAgent()
					.orElseThrow(() -> new InvalidPropertiesException("Managing agent AID was not given."));
			send(prepareMessageToManagingAgent(getContainerController().getContainerName(), getLocalName(),
					managingAgent));
		} catch (ControllerException e) {
			throw new JadeContainerException("Container not found!", e);
		}
	}
}
