package org.greencloud.strategyinjection.agentsystem.ruleset.restaurant.service;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_LISTENER_HANDLER;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_PROPOSAL_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.greencloud.strategyinjection.agentsystem.domain.ClientOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gui.agents.AgentNode;

public class ProcessNewClientOrdersRule extends AgentBasicRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	private static final Logger logger = LoggerFactory.getLogger(ProcessNewClientOrdersRule.class);

	public ProcessNewClientOrdersRule(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(BASIC_LISTENER_HANDLER,
				"process new client orders",
				"processing new client restaurant look-up orders");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientOrder clientOrder = facts.get(MESSAGE_CONTENT);

		if (agentProps.canFulfillOrder(clientOrder)) {
			return true;
		}
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
		return false;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Sending proposal for order {} to booking agent!",
				((ClientOrder) facts.get(MESSAGE_CONTENT)).getOrderId());
		agent.addBehaviour(InitiateProposal.create(agent, facts, BASIC_PROPOSAL_RULE, controller));
	}
}