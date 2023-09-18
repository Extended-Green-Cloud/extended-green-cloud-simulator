package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_FAILURE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_FAILURE_RESULTS_MESSAGES;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_INFORM_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_INFORM_RESULTS_MESSAGES;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_REFUSE_MESSAGE;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_ALL_RESULTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_FAILURE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_INFORM_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_REFUSE_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.REQUEST;

import java.util.Collection;
import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Request initiator behaviour
 */
public abstract class AgentRequestRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentRequestRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return REQUEST;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(
				new CreateRequestMessageRule(),
				new HandleInformRule(),
				new HandleRefuseRule(),
				new HandleFailureRule(),
				new HandleAllResponsesRule());
	}

	/**
	 * Method executed when request message is to be created
	 */
	protected abstract ACLMessage createRequestMessage(final StrategyFacts facts);

	/**
	 * Method evaluates if the action should be executed upon any message received
	 */
	protected boolean evaluateBeforeForAll(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method evaluates if the action should be executed upon all messages received
	 */
	protected boolean evaluateBeforeForAllResults(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method evaluates if the action should be executed when inform message is received
	 */
	protected boolean evaluateBeforeInform(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method evaluates if the action should be executed when refuse message is received
	 */
	protected boolean evaluateBeforeRefuse(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method evaluates if the action should be executed when failure message is received
	 */
	protected boolean evaluateBeforeFailure(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method executed when INFORM message is to be handled
	 */
	protected abstract void handleInform(final ACLMessage inform, final StrategyFacts facts);

	/**
	 * Method executed when REFUSE message is to be handled
	 */
	protected abstract void handleRefuse(final ACLMessage refuse, final StrategyFacts facts);

	/**
	 * Method executed when FAILURE message is to be handled
	 */
	protected abstract void handleFailure(final ACLMessage failure, final StrategyFacts facts);

	/**
	 * Optional method executed when ALL RESULT messages are to be handled
	 */
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final StrategyFacts facts) {

	}

	// RULE EXECUTED WHEN REQUEST MESSAGE IS TO BE CREATED
	class CreateRequestMessageRule extends AgentBasicRule<T, E> {

		public CreateRequestMessageRule() {
			super(AgentRequestRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage proposal = createRequestMessage(facts);
			facts.put(REQUEST_CREATE_MESSAGE, proposal);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentRequestRule.this.ruleType, REQUEST_CREATE_STEP,
					format("%s - create request message", AgentRequestRule.this.name),
					"rule performed when request message sent to other agents is to be created");
		}
	}

	// RULE EXECUTED WHEN INFORM MESSAGE IS RECEIVED
	class HandleInformRule extends AgentBasicRule<T, E> {

		public HandleInformRule() {
			super(AgentRequestRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final StrategyFacts facts) {
			return evaluateBeforeForAll(facts) && evaluateBeforeInform(facts);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage inform = facts.get(REQUEST_INFORM_MESSAGE);
			handleInform(inform, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentRequestRule.this.ruleType, REQUEST_HANDLE_INFORM_STEP,
					format("%s - handle inform message", AgentRequestRule.this.name),
					"rule that handles case when INFORM message is received");
		}
	}

	// RULE EXECUTED WHEN REFUSE MESSAGE IS RECEIVED
	class HandleRefuseRule extends AgentBasicRule<T, E> {

		public HandleRefuseRule() {
			super(AgentRequestRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final StrategyFacts facts) {
			return evaluateBeforeForAll(facts) && evaluateBeforeRefuse(facts);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage refuse = facts.get(REQUEST_REFUSE_MESSAGE);
			handleRefuse(refuse, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentRequestRule.this.ruleType, REQUEST_HANDLE_REFUSE_STEP,
					format("%s - handle refuse message", AgentRequestRule.this.name),
					"rule that handles case when REFUSE message is received");
		}
	}

	// RULE EXECUTED WHEN FAILURE MESSAGE IS RECEIVED
	class HandleFailureRule extends AgentBasicRule<T, E> {

		public HandleFailureRule() {
			super(AgentRequestRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final StrategyFacts facts) {
			return evaluateBeforeForAll(facts) && evaluateBeforeFailure(facts);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage failure = facts.get(REQUEST_FAILURE_MESSAGE);
			handleFailure(failure, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentRequestRule.this.ruleType, REQUEST_HANDLE_FAILURE_STEP,
					format("%s - handle failure message", AgentRequestRule.this.name),
					"rule that handles case when FAILURE message is received");
		}
	}

	// RULE EXECUTED WHEN ALL FAILURE AND INFORM MESSAGES ARE RECEIVED
	class HandleAllResponsesRule extends AgentBasicRule<T, E> {

		public HandleAllResponsesRule() {
			super(AgentRequestRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final StrategyFacts facts) {
			return evaluateBeforeForAll(facts) && evaluateBeforeForAllResults(facts);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final Collection<ACLMessage> informResults = facts.get(REQUEST_INFORM_RESULTS_MESSAGES);
			final Collection<ACLMessage> failureResults = facts.get(REQUEST_FAILURE_RESULTS_MESSAGES);
			handleAllResults(informResults, failureResults, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentRequestRule.this.ruleType, REQUEST_HANDLE_ALL_RESULTS_STEP,
					format("%s - handle all messages", AgentRequestRule.this.name),
					"rule that handles case when all INFORM and FAILURE messages are received");
		}
	}

}
