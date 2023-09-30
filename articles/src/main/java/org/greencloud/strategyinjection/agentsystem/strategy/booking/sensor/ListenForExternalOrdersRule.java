package org.greencloud.strategyinjection.agentsystem.strategy.booking.sensor;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_CFP_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.strategy.StrategyType.DEFAULT_STRATEGY;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateCallForProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.greencloud.strategyinjection.agentsystem.domain.ClientOrder;
import org.greencloud.strategyinjection.agentsystem.domain.RestaurantOfferResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.lang.acl.ACLMessage;

public class ListenForExternalOrdersRule extends AgentPeriodicRule<BookingProps, BookingNode> {

	private static final Logger logger = LoggerFactory.getLogger(ListenForExternalOrdersRule.class);
	private static final long TIMEOUT = 100;

	public ListenForExternalOrdersRule(final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SENSE_EVENTS_RULE,
				"listen for external client orders",
				"rule listens for external client orders");
	}

	@Override
	protected long specifyPeriod() {
		return TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final StrategyFacts facts) {
		return nonNull(agentNode);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		final Optional<Pair<String, Object>> latestEvent = ofNullable(agentNode.getClientEvents().poll());

		latestEvent.ifPresent(event -> {
			if (event.getKey().equals("RESTAURANT_LOOK_UP")) {
				final ClientOrder order = (ClientOrder) event.getValue();

				if (Strings.isNotBlank(order.getAdditionalInstructions())) {
					controller.addModifiedStrategy(order.getAdditionalInstructions(),
							controller.getLatestStrategy().incrementAndGet());
					logger.info("Customer added personalized search instructions! Changing strategy to {}.",
							order.getAdditionalInstructions());
				}

				facts.put(STRATEGY_IDX, controller.getLatestStrategy().get());
				agentProps.getStrategyForOrder()
						.put(Integer.toString(order.getOrderId()), controller.getLatestStrategy().get());
				logger.info("New client order with id {} was received. Looking for restaurants with strategy {}.",
						order.getOrderId(),
						controller.getStrategies().get(controller.getLatestStrategy().get()).getName());
				facts.put(RESULT, order);
				agent.addBehaviour(InitiateCallForProposal.create(agent, facts, BASIC_CFP_RULE, controller));
			} else {
				final RestaurantOfferResponseMessage response = (RestaurantOfferResponseMessage) event.getValue();
				final ACLMessage restaurantMsg = agentProps.getRestaurantForOrder().get(response.getOrderId());
				agentProps.getRestaurantForOrder().remove(response.getOrderId());

				final int strategyIdx = agentProps.getStrategyForOrder()
						.remove(Integer.toString(response.getOrderId()));
				controller.removeStrategy(agentProps.getStrategyForOrder(), strategyIdx);
				controller.addNewStrategy(DEFAULT_STRATEGY.name(), controller.getLatestStrategy().incrementAndGet());

				final ACLMessage message = response.getAccepted() ?
						prepareStringReply(restaurantMsg, "ACCEPT", ACCEPT_PROPOSAL) :
						prepareStringReply(restaurantMsg, "REJECT", REJECT_PROPOSAL);
				agent.send(message);
			}
		});
	}
}
