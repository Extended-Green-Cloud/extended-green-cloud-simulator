package org.greencloud.strategyinjection.agentsystem.ruleset.restaurant.service;

import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_LISTENER;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_LISTENER_HANDLER;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.greencloud.strategyinjection.agentsystem.domain.ClientOrder;

import com.gui.agents.AgentNode;

import jade.lang.acl.MessageTemplate;

public class ListenForNewClientOrdersRule extends
		AgentMessageListenerRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	private static final MessageTemplate ORDER_TEMPLATE =
			and(MatchProtocol("NEW_CLIENT_ORDER_PROTOCOL"), MatchPerformative(CFP));

	public ListenForNewClientOrdersRule(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientOrder.class, ORDER_TEMPLATE, 1, BASIC_LISTENER_HANDLER);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(BASIC_LISTENER,
				"listen for new client orders",
				"listening for new client restaurant look-up orders");
	}
}