package org.greencloud.strategyinjection.agentsystem.strategy.restaurant.service;

import static jade.lang.acl.ACLMessage.PROPOSE;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_PROPOSAL_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.greencloud.strategyinjection.agentsystem.domain.ClientOrder;
import org.greencloud.strategyinjection.agentsystem.domain.ImmutableRestaurantData;
import org.greencloud.strategyinjection.agentsystem.domain.RestaurantData;
import org.slf4j.Logger;

import com.gui.agents.AgentNode;

import jade.lang.acl.ACLMessage;

public class ProposeToBookingServiceRule
		extends AgentProposalRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	private static final Logger logger = getLogger(ProposeToBookingServiceRule.class);

	public ProposeToBookingServiceRule(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(BASIC_PROPOSAL_RULE,
				"propose offer with dish price and restaurant information",
				"rule sends proposal message to Booking Agent and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final StrategyFacts facts) {
		final ClientOrder clientOrder = facts.get(MESSAGE_CONTENT);
		final double price = agentProps.getDishWithPrice().get(clientOrder.getDish());
		final Map<String, Object> restaurantInfo =
				ofNullable(agentProps.getAdditionalInformation()).orElse(new HashMap<>());
		restaurantInfo.put("cuisine", agentProps.getCuisineType().name());
		restaurantInfo.put("dish", clientOrder.getDish());

		final RestaurantData responseData = ImmutableRestaurantData.builder()
				.restaurantInformation(restaurantInfo)
				.price(price)
				.build();

		return prepareReply(facts.get(MESSAGE), responseData, PROPOSE);
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final StrategyFacts facts) {
		logger.info("Booking Agent accepted my offer!");
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final StrategyFacts facts) {
		logger.info("Booking Agent rejected my offer!");
	}
}
