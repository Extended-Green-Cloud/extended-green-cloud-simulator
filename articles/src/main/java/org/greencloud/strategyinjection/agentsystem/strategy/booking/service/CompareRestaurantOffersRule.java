package org.greencloud.strategyinjection.agentsystem.strategy.booking.service;

import static org.greencloud.commons.constants.FactTypeConstants.CFP_BEST_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_COMPARATOR_RULE;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;

import java.util.Comparator;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.greencloud.strategyinjection.agentsystem.domain.RestaurantData;

import jade.lang.acl.ACLMessage;

public class CompareRestaurantOffersRule extends AgentBasicRule<BookingProps, BookingNode> {

	public CompareRestaurantOffersRule(
			final RulesController<BookingProps, BookingNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(BASIC_COMPARATOR_RULE,
				"compare offers received from restaurants",
				"compare offers received from restaurants");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ACLMessage bestProposal = facts.get(CFP_BEST_MESSAGE);
		final ACLMessage newProposal = facts.get(MESSAGE);

		final Comparator<RestaurantData> comparator = (msg1, msg2) -> {
			final double priceDiff = msg2.getPrice() - msg1.getPrice();
			return (int) priceDiff;
		};

		facts.put(RESULT, compareMessages(bestProposal, newProposal, RestaurantData.class, comparator));
	}
}
