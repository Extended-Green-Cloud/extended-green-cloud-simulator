package org.greencloud.strategyinjection.agentsystem.ruleset.booking.sensor;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_CFP_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_RULE_SET;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.greencloud.commons.domain.facts.RuleSetFacts;
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
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return nonNull(agentNode);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final Optional<Pair<String, Object>> latestEvent = ofNullable(agentNode.getClientEvents().poll());

		latestEvent.ifPresent(event -> {
			if (event.getKey().equals("RESTAURANT_LOOK_UP")) {
				final ClientOrder order = (ClientOrder) event.getValue();

				if (Strings.isNotBlank(order.getAdditionalInstructions())) {
					controller.addModifiedRuleSet(order.getAdditionalInstructions(),
							controller.getLatestRuleSet().incrementAndGet());
					logger.info("Customer added personalized search instructions! Changing rule set to {}.",
							order.getAdditionalInstructions());
				}

				facts.put(RULE_SET_IDX, controller.getLatestRuleSet().get());
				agentProps.getStrategyForOrder()
						.put(Integer.toString(order.getOrderId()), controller.getLatestRuleSet().get());
				logger.info("New client order with id {} was received. Looking for restaurants with rule set {}.",
						order.getOrderId(),
						controller.getRuleSets().get(controller.getLatestRuleSet().get()).getName());
				facts.put(RESULT, order);
				agent.addBehaviour(InitiateCallForProposal.create(agent, facts, BASIC_CFP_RULE, controller));
			} else {
				final RestaurantOfferResponseMessage response = (RestaurantOfferResponseMessage) event.getValue();
				final ACLMessage restaurantMsg = agentProps.getRestaurantForOrder().get(response.getOrderId());
				agentProps.getRestaurantForOrder().remove(response.getOrderId());

				final int strategyIdx = agentProps.getStrategyForOrder()
						.remove(Integer.toString(response.getOrderId()));
				controller.removeRuleSet(agentProps.getStrategyForOrder(), strategyIdx);
				controller.addNewRuleSet(DEFAULT_RULE_SET, controller.getLatestRuleSet().incrementAndGet());

				final ACLMessage message = response.getAccepted() ?
						prepareStringReply(restaurantMsg, "ACCEPT", ACCEPT_PROPOSAL) :
						prepareStringReply(restaurantMsg, "REJECT", REJECT_PROPOSAL);
				agent.send(message);
			}
		});
	}
}
