package org.greencloud.agentsystem.agents;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.AgentType.CLIENT;
import static org.greencloud.commons.args.agent.AgentType.MANAGING;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.greencloud.commons.constants.LoggingConstants.MDC_CLIENT_NAME;
import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_CLOUD_RULE_SET;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareMessageToManagingAgent;

import java.util.List;
import java.util.Map;

import org.greencloud.agentsystem.behaviours.ListenForControllerObjects;
import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.args.agent.AgentNodeProps;
import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.exception.JadeContainerException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.rulescontroller.RulesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.database.knowledge.domain.action.AdaptationActionEnum;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class representing agent which has the connection with GUI controller
 */
@SuppressWarnings("unchecked")
@Getter
@Setter
public abstract class AbstractAgent<T extends EGCSNode<?, E>, E extends AgentProps> extends Agent {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAgent.class);

	protected T agentNode;
	protected E properties;
	protected ParallelBehaviour mainBehaviour;
	protected RulesController<E, T> rulesController;

	protected AbstractAgent() {
		setEnabledO2ACommunication(true, 2);
	}

	/**
	 * Abstract method used to validate if arguments of the given agent are correct
	 */
	protected void validateAgentArguments() {
	}

	/**
	 * Abstract method used to initialize given agent data
	 *
	 * @param arguments arguments passed by the user
	 */
	protected void initializeAgent(final Object[] arguments) {
	}

	/**
	 * Abstract method that is used to prepare starting behaviours for given agent
	 */
	protected List<Behaviour> prepareStartingBehaviours() {
		return emptyList();
	}

	/**
	 * Abstract method responsible for running starting behaviours
	 */
	protected void runStartingBehaviours() {
		addBehaviour(new ListenForControllerObjects(this, prepareStartingBehaviours()));
	}

	/**
	 * Abstract method responsible for running initial custom behaviours prepared only for selected rule set
	 */
	protected void runInitialBehavioursForRuleSet() {
		final RuleSetFacts facts = new RuleSetFacts(rulesController.getLatestRuleSet().get());
		facts.put(RULE_TYPE, INITIALIZE_BEHAVIOURS_RULE);
		rulesController.fire(facts);
	}

	/**
	 * Abstract method invoked when the agent is the target of adaptation
	 *
	 * @param adaptationActionEnum adaptation action type
	 * @param actionParameters     parameters related with given adaptation
	 * @return flag indicating if adaptation was successful
	 */
	public boolean executeAction(final AdaptationActionEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters) {
		if (nonNull(rulesController)) {
			final RuleSetFacts facts = new RuleSetFacts(rulesController.getLatestRuleSet().get());
			facts.put(RULE_TYPE, ADAPTATION_REQUEST_RULE);
			facts.put(ADAPTATION_PARAMS, actionParameters);
			facts.put(ADAPTATION_TYPE, adaptationActionEnum);
			rulesController.fire(facts);
			return facts.get(RESULT);
		} else {
			logger.info("Cannot execute adaptation - rules controller has not been initialized.");
			return false;
		}
	}

	/**
	 * Abstract method invoked when the agent is the target of adaptation and the adaptation requires communicating
	 * with other agents (i.e. cannot be executed on the spot)
	 *
	 * @param adaptationActionEnum adaptation action type
	 * @param actionParameters     parameters related with given adaptation
	 * @param adaptationMessage    message with adaptation request
	 */
	public void executeAction(final AdaptationActionEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters,
			final ACLMessage adaptationMessage) {
		if (nonNull(rulesController)) {
			final RuleSetFacts facts = new RuleSetFacts(rulesController.getLatestRuleSet().get());
			facts.put(MESSAGE, adaptationMessage);
			facts.put(RULE_TYPE, ADAPTATION_REQUEST_RULE);
			facts.put(ADAPTATION_PARAMS, actionParameters);
			facts.put(ADAPTATION_TYPE, adaptationActionEnum);
			rulesController.fire(facts);
		} else {
			logger.info("Cannot execute adaptation - rules controller has not been initialized.");
		}
	}

	/**
	 * Method used to select rule based on given fact
	 *
	 * @param facts set of facts based on which given rule is triggered
	 */
	public void fireOnFacts(final RuleSetFacts facts) {
		rulesController.fire(facts);
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
		if (properties.getAgentType().equals(CLIENT.name())) {
			MDC.put(MDC_CLIENT_NAME, super.getLocalName());
		} else {
			MDC.put(MDC_AGENT_NAME, super.getLocalName());
		}
		final Object[] arguments = getArguments();

		initializeAgent(arguments);
		validateAgentArguments();
		runStartingBehaviours();

		if (arguments.length >= 3 && !List.of(CLIENT.name(), MANAGING.name()).contains(properties.getAgentType())) {
			properties.setSystemKnowledge((Map<String, Map<String, Object>>) arguments[arguments.length - 3]);
		}

		// checking if the managing agent should be informed about agent creation
		if (arguments.length > 0 && !List.of(CLIENT.name(), MANAGING.name()).contains(properties.getAgentType())
				&& (boolean) arguments[arguments.length - 2]) {
			try {
				final AID managingAgent = (AID) arguments[arguments.length - 1];
				send(prepareMessageToManagingAgent(getContainerController().getContainerName(), getLocalName(),
						managingAgent));
			} catch (ControllerException e) {
				throw new JadeContainerException("Container not found!", e);
			}
		}
	}

	@Override
	protected void takeDown() {
		if (properties.getAgentType().equals(CLIENT.name())) {
			MDC.put(MDC_CLIENT_NAME, super.getLocalName());
		} else {
			MDC.put(MDC_AGENT_NAME, super.getLocalName());
		}

		logger.info("I'm finished. Bye!");
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

	public void setRulesController(RulesController<E, T> rulesController) {
		this.rulesController = rulesController;
		properties.setAgentName(getName());
		properties.setAgentNode((AgentNodeProps<AgentProps>) agentNode);
		rulesController.setAgent(this, properties, agentNode, DEFAULT_CLOUD_RULE_SET);
		runInitialBehavioursForRuleSet();
	}
}
