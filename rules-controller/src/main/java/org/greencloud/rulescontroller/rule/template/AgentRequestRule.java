package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
import static org.greencloud.rulescontroller.rule.AgentRuleType.REQUEST;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.RequestRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Request initiator behaviour
 */
public class AgentRequestRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionCreateRequestMessage;
	private Serializable expressionEvaluateBeforeForAll;
	private Serializable expressionHandleInform;
	private Serializable expressionHandleRefuse;
	private Serializable expressionHandleFailure;
	private Serializable expressionHandleAllResults;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentRequestRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentRequestRule(final RequestRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getCreateRequestMessage())) {
			this.expressionCreateRequestMessage = MVEL.compileExpression(
					imports + " " + ruleRest.getCreateRequestMessage());
		}
		if (nonNull(ruleRest.getEvaluateBeforeForAll())) {
			this.expressionEvaluateBeforeForAll = MVEL.compileExpression(
					imports + " " + ruleRest.getEvaluateBeforeForAll());
		}
		if (nonNull(ruleRest.getHandleInform())) {
			this.expressionHandleInform = MVEL.compileExpression(imports + " " + ruleRest.getHandleInform());
		}
		if (nonNull(ruleRest.getHandleFailure())) {
			this.expressionHandleFailure = MVEL.compileExpression(imports + " " + ruleRest.getHandleFailure());
		}
		if (nonNull(ruleRest.getHandleAllResults())) {
			this.expressionHandleAllResults = MVEL.compileExpression(imports + " " + ruleRest.getHandleAllResults());
		}
		if (nonNull(ruleRest.getHandleRefuse())) {
			this.expressionHandleRefuse = MVEL.compileExpression(imports + " " + ruleRest.getHandleRefuse());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(
				new CreateRequestMessageRule(),
				new HandleInformRule(),
				new HandleRefuseRule(),
				new HandleFailureRule(),
				new HandleAllResponsesRule()));
	}

	@Override
	public List<AgentRule> getRules() {
		return stepRules;
	}

	@Override
	public void connectToController(final RulesController<?, ?> rulesController) {
		super.connectToController(rulesController);
		stepRules.forEach(rule -> rule.connectToController(rulesController));
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return REQUEST;
	}


	/**
	 * Method executed when request message is to be created
	 */
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		return null;
	}

	/**
	 * Method evaluates if the action should be executed upon any message received
	 */
	protected boolean evaluateBeforeForAll(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method executed when INFORM message is to be handled
	 */
	protected void handleInform(final ACLMessage inform, final StrategyFacts facts) {
	}

	/**
	 * Method executed when REFUSE message is to be handled
	 */
	protected void handleRefuse(final ACLMessage refuse, final StrategyFacts facts) {
	}

	/**
	 * Method executed when FAILURE message is to be handled
	 */
	protected void handleFailure(final ACLMessage failure, final StrategyFacts facts) {
	}

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
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			final ACLMessage request = isNull(expressionCreateRequestMessage) ?
					createRequestMessage(facts) :
					(ACLMessage) MVEL.executeExpression(expressionCreateRequestMessage,
							AgentRequestRule.this.initialParameters);
			facts.put(REQUEST_CREATE_MESSAGE, request);
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
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			return isNull(expressionEvaluateBeforeForAll) ?
					evaluateBeforeForAll(facts) :
					(boolean) MVEL.executeExpression(expressionEvaluateBeforeForAll,
							AgentRequestRule.this.initialParameters);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage inform = facts.get(REQUEST_INFORM_MESSAGE);
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleInform)) {
				handleInform(inform, facts);
			} else {
				AgentRequestRule.this.initialParameters.put("inform", inform);
				MVEL.executeExpression(expressionHandleInform, AgentRequestRule.this.initialParameters);
				AgentRequestRule.this.initialParameters.remove("inform");
			}
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
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			return isNull(expressionEvaluateBeforeForAll) ?
					evaluateBeforeForAll(facts) :
					(boolean) MVEL.executeExpression(expressionEvaluateBeforeForAll,
							AgentRequestRule.this.initialParameters);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage refuse = facts.get(REQUEST_REFUSE_MESSAGE);
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleRefuse)) {
				handleRefuse(refuse, facts);
			} else {
				AgentRequestRule.this.initialParameters.put("refuse", refuse);
				MVEL.executeExpression(expressionHandleRefuse, AgentRequestRule.this.initialParameters);
				AgentRequestRule.this.initialParameters.remove("refuse");
			}
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
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			return isNull(expressionEvaluateBeforeForAll) ?
					evaluateBeforeForAll(facts) :
					(boolean) MVEL.executeExpression(expressionEvaluateBeforeForAll,
							AgentRequestRule.this.initialParameters);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage failure = facts.get(REQUEST_FAILURE_MESSAGE);
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleFailure)) {
				handleFailure(failure, facts);
			} else {
				AgentRequestRule.this.initialParameters.put("failure", failure);
				MVEL.executeExpression(expressionHandleFailure, AgentRequestRule.this.initialParameters);
				AgentRequestRule.this.initialParameters.remove("failure");
			}
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
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			return isNull(expressionEvaluateBeforeForAll) ?
					evaluateBeforeForAll(facts) :
					(boolean) MVEL.executeExpression(expressionEvaluateBeforeForAll,
							AgentRequestRule.this.initialParameters);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final Collection<ACLMessage> informResults = facts.get(REQUEST_INFORM_RESULTS_MESSAGES);
			final Collection<ACLMessage> failureResults = facts.get(REQUEST_FAILURE_RESULTS_MESSAGES);
			if (nonNull(AgentRequestRule.this.initialParameters)) {
				AgentRequestRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleAllResults)) {
				handleAllResults(informResults, failureResults, facts);
			} else {
				AgentRequestRule.this.initialParameters.put("informResults", informResults);
				AgentRequestRule.this.initialParameters.put("failureResults", failureResults);
				MVEL.executeExpression(expressionHandleAllResults, AgentRequestRule.this.initialParameters);
				AgentRequestRule.this.initialParameters.remove("informResults");
				AgentRequestRule.this.initialParameters.remove("failureResults");
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentRequestRule.this.ruleType, REQUEST_HANDLE_ALL_RESULTS_STEP,
					format("%s - handle all messages", AgentRequestRule.this.name),
					"rule that handles case when all INFORM and FAILURE messages are received");
		}
	}

}
