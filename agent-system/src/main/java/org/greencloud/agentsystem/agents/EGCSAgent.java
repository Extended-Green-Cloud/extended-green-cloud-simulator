package org.greencloud.agentsystem.agents;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.args.agent.EGCSAgentType.MANAGING;
import static org.greencloud.commons.utils.facts.AdaptationFactsFactory.constructFactsForAdaptationRequest;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareMessageToManagingAgent;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_CLIENT_NAME;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.greencloud.agentsystem.behaviours.ListenForAdaptationAction;
import org.greencloud.agentsystem.behaviours.ReportHealthCheck;
import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.greencloud.commons.exception.JadeContainerException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.jrba.agentmodel.behaviour.ListenForControllerObjects;
import org.jrba.agentmodel.domain.AbstractAgent;
import org.jrba.agentmodel.domain.props.AgentProps;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class representing agent which has the connection with GUI controller.
 */
@SuppressWarnings("unchecked")
@Getter
@Setter
public abstract class EGCSAgent<T extends EGCSNode<?, E>, E extends AgentProps> extends AbstractAgent<T, E> {

	private static final Logger logger = LoggerFactory.getLogger(EGCSAgent.class);
	private static Predicate<AgentProps> isCloudAgent = props ->
			!List.of(CLIENT.name(), MANAGING.name()).contains(props.getAgentType());

	protected ParallelBehaviour mainBehaviour;

	/**
	 * Abstract method invoked when the agent is the target of adaptation.
	 *
	 * @param adaptationActionEnum adaptation action type
	 * @param actionParameters     parameters related with given adaptation
	 * @return flag indicating if adaptation was successful
	 */
	public boolean executeAction(final AdaptationActionTypeEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters) {
		if (nonNull(rulesController)) {
			final RuleSetFacts facts = constructFactsForAdaptationRequest(
					rulesController.getLatestLongTermRuleSetIdx().get(), adaptationActionEnum, actionParameters);
			rulesController.fire(facts);
			return facts.get(RESULT);
		} else {
			logger.info("Cannot execute adaptation - rules controller has not been initialized.");
			return false;
		}
	}

	/**
	 * Abstract method invoked when the agent is the target of adaptation and the adaptation requires communicating
	 * with other agents (i.e. cannot be executed on the spot).
	 *
	 * @param adaptationActionEnum adaptation action type
	 * @param actionParameters     parameters related with given adaptation
	 * @param adaptationMessage    message with adaptation request
	 */
	public void executeAction(final AdaptationActionTypeEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters,
			final ACLMessage adaptationMessage) {
		if (nonNull(rulesController)) {
			final RuleSetFacts facts = constructFactsForAdaptationRequest(
					rulesController.getLatestLongTermRuleSetIdx().get(), adaptationActionEnum, actionParameters);
			facts.put(MESSAGE, adaptationMessage);
			rulesController.fire(facts);
		} else {
			logger.info("Cannot execute adaptation - rules controller has not been initialized.");
		}
	}

	@Override
	public void setRulesController(RulesController<E, T> rulesController) {
		this.rulesController = rulesController;
		properties.setAgentName(getName());
		if (nonNull(agentNode)) {
			properties.setAgentNode(agentNode);
		}
		rulesController.setAgent(this, properties, agentNode, getDefaultRuleSet());
	}

	@Override
	protected int getObjectsNumber() {
		return 2;
	}

	@Override
	protected void runStartingBehaviours() {
		addBehaviour(new ListenForControllerObjects(this, prepareStartingBehaviours(), getObjectsNumber(),
				initializeBehavioursWhenAgentIsConnected()));

	}

	@Override
	public void clean(boolean ok) {
		if (!ok && nonNull(getAgentNode()) && !properties.getAgentType().equals(CLIENT.name())) {
			getAgentNode().removeAgentNodeFromGraph();
		}
		super.clean(ok);
	}

	@Override
	protected void setup() {
		logger.info("Setting up Agent {}", getName());
		setUpLogger();

		final Object[] arguments = getArguments();
		super.setup();

		if (arguments.length >= 3 && isCloudAgent.test(properties)) {
			updateKnowledgeMap(arguments);
		}

		// checking if the managing agent should be informed about agent creation
		if (arguments.length > 0 && isCloudAgent.test(properties) && (boolean) arguments[arguments.length - 2]) {
			informManagingAboutContainerInitialization(arguments);
		}
	}

	@Override
	protected void takeDown() {
		setUpLogger();
		super.takeDown();
	}

	@Override
	public void addBehaviour(Behaviour b) {
		if (nonNull(mainBehaviour) && !mainBehaviour.equals(b)) {
			mainBehaviour.addSubBehaviour(b);
		} else {
			super.addBehaviour(b);
		}
	}

	private void setUpLogger() {
		final String loggerName = properties.getAgentType().equals(CLIENT.name()) ? MDC_CLIENT_NAME : MDC_AGENT_NAME;
		MDC.put(loggerName, super.getLocalName());
	}

	private Consumer<List<Behaviour>> initializeBehavioursWhenAgentIsConnected() {
		return initialBehaviours -> {
			final ParallelBehaviour behaviour = new ParallelBehaviour();
			initialBehaviours.forEach(behaviour::addSubBehaviour);
			behaviour.addSubBehaviour(new ReportHealthCheck(this));
			behaviour.addSubBehaviour(new ListenForAdaptationAction(this));
			addBehaviour(behaviour);
			setMainBehaviour(behaviour);
			runInitialBehavioursForRuleSet();
		};
	}

	private void updateKnowledgeMap(final Object[] arguments) {
		final Optional<Map<String, Map<String, Object>>> knowledgeMap =
				(Optional<Map<String, Map<String, Object>>>) arguments[arguments.length - 3];
		knowledgeMap.ifPresent(map -> properties.setSystemKnowledge(map));
	}

	private void informManagingAboutContainerInitialization(final Object[] arguments) {
		try {
			final AID managingAgent = (AID) arguments[arguments.length - 1];
			send(prepareMessageToManagingAgent(getContainerController().getContainerName(), getLocalName(),
					managingAgent));
		} catch (ControllerException e) {
			throw new JadeContainerException("Container not found!", e);
		}
	}
}
