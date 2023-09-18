package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_ADDED_AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_REMOVED_AGENTS;
import static org.greencloud.commons.enums.rules.RuleStepType.SUBSCRIPTION_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SUBSCRIPTION_HANDLE_AGENTS_RESPONSE_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SUBSCRIPTION;

import java.util.List;
import java.util.Map;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Subscription behaviour
 */
public abstract class AgentSubscriptionRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentSubscriptionRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return SUBSCRIPTION;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(new CreateSubscriptionRule(), new HandleDFInformMessage());
	}

	/**
	 * Method which can be optionally overridden in order to read common fact objects
	 */
	protected void readConstantFacts(final StrategyFacts facts) {
	}

	/**
	 * Method executed when subscription message is to be created
	 */
	protected abstract ACLMessage createSubscriptionMessage(final StrategyFacts facts);

	/**
	 * Method handles removing agents which deregistered their service
	 */
	protected abstract void handleRemovedAgents(final Map<AID, Boolean> removedAgents);

	/**
	 * Method handles adding new agents which registered their service
	 */
	protected abstract void handleAddedAgents(final Map<AID, Boolean> addedAgents);

	// RULE EXECUTED WHEN SUBSCRIPTION MESSAGE IS TO BE CREATED
	class CreateSubscriptionRule extends AgentBasicRule<T, E> {

		public CreateSubscriptionRule() {
			super(AgentSubscriptionRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			readConstantFacts(facts);
			final ACLMessage cfp = createSubscriptionMessage(facts);
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

			if (!addedAgents.isEmpty()) {
				handleAddedAgents(addedAgents);
			}
			if (!removedAgents.isEmpty()) {
				handleRemovedAgents(removedAgents);
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
