package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_ADDED_AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_REMOVED_AGENTS;
import static org.greencloud.commons.enums.rules.RuleStepType.SUBSCRIPTION_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SUBSCRIPTION_HANDLE_AGENTS_RESPONSE_STEP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SUBSCRIPTION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.SubscriptionRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Subscription behaviour
 */
public class AgentSubscriptionRule<T extends AgentProps, E extends AgentNode<T>>
		extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionCreateSubscriptionMessage;
	private Serializable expressionHandleRemovedAgents;
	private Serializable expressionHandleAddedAgents;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentSubscriptionRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentSubscriptionRule(final SubscriptionRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getCreateSubscriptionMessage())) {
			this.expressionCreateSubscriptionMessage = MVEL.compileExpression(
					imports + " " + ruleRest.getCreateSubscriptionMessage());
		}
		if (nonNull(ruleRest.getHandleAddedAgents())) {
			this.expressionHandleAddedAgents = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleAddedAgents());
		}
		if (nonNull(ruleRest.getHandleRemovedAgents())) {
			this.expressionHandleRemovedAgents = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleRemovedAgents());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(new CreateSubscriptionRule(), new HandleDFInformMessage()));
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
		return SUBSCRIPTION;
	}


	/**
	 * Method executed when subscription message is to be created
	 */
	protected ACLMessage createSubscriptionMessage(final StrategyFacts facts) {
		return null;
	}

	/**
	 * Method handles removing agents which deregistered their service
	 */
	protected void handleRemovedAgents(final Map<AID, Boolean> removedAgents) {
	}

	/**
	 * Method handles adding new agents which registered their service
	 */
	protected void handleAddedAgents(final Map<AID, Boolean> addedAgents) {
	}

	// RULE EXECUTED WHEN SUBSCRIPTION MESSAGE IS TO BE CREATED
	class CreateSubscriptionRule extends AgentBasicRule<T, E> {

		public CreateSubscriptionRule() {
			super(AgentSubscriptionRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			if (nonNull(AgentSubscriptionRule.this.initialParameters)) {
				AgentSubscriptionRule.this.initialParameters.replace("facts", facts);
			}

			final ACLMessage cfp = isNull(expressionCreateSubscriptionMessage) ?
					createSubscriptionMessage(facts) :
					(ACLMessage) MVEL.executeExpression(expressionCreateSubscriptionMessage,
							AgentSubscriptionRule.this.initialParameters);
			facts.put(SUBSCRIPTION_CREATE_MESSAGE, cfp);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSubscriptionRule.this.ruleType, SUBSCRIPTION_CREATE_STEP,
					format("%s - create subscription message", AgentSubscriptionRule.this.name),
					"when agent initiate DF subscription, it creates subscription message");
		}
	}

	// RULE EXECUTED WHEN RESPONSE IS RECEIVED FROM DF
	class HandleDFInformMessage extends AgentBasicRule<T, E> {

		public HandleDFInformMessage() {
			super(AgentSubscriptionRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final Map<AID, Boolean> addedAgents = facts.get(SUBSCRIPTION_ADDED_AGENTS);
			final Map<AID, Boolean> removedAgents = facts.get(SUBSCRIPTION_REMOVED_AGENTS);
			if (nonNull(AgentSubscriptionRule.this.initialParameters)) {
				AgentSubscriptionRule.this.initialParameters.replace("facts", facts);
			}

			if (!addedAgents.isEmpty()) {
				if (isNull(expressionHandleAddedAgents)) {
					handleAddedAgents(addedAgents);
				} else {
					AgentSubscriptionRule.this.initialParameters.put("addedAgents", addedAgents);
					MVEL.executeExpression(expressionHandleAddedAgents, AgentSubscriptionRule.this.initialParameters);
					AgentSubscriptionRule.this.initialParameters.remove("addedAgents");
				}
			}
			if (!removedAgents.isEmpty()) {
				if (isNull(expressionHandleRemovedAgents)) {
					handleRemovedAgents(removedAgents);
				} else {
					AgentSubscriptionRule.this.initialParameters.put("removedAgents", removedAgents);
					MVEL.executeExpression(expressionHandleRemovedAgents, AgentSubscriptionRule.this.initialParameters);
					AgentSubscriptionRule.this.initialParameters.remove("removedAgents");
				}
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSubscriptionRule.this.ruleType,
					SUBSCRIPTION_HANDLE_AGENTS_RESPONSE_STEP,
					format("%s - handle changes in subscribed service", AgentSubscriptionRule.this.name),
					"when DF sends information about changes in subscribed service, agent executes default"
							+ "handlers");
		}
	}

}
