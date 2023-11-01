package org.greencloud.strategyinjection.agentsystem.ruleset;

import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_RULE_SET;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.greencloud.strategyinjection.agentsystem.ruleset.booking.df.SearchForRestaurantsRule;
import org.greencloud.strategyinjection.agentsystem.ruleset.booking.initial.StartInitialBookingBehaviours;
import org.greencloud.strategyinjection.agentsystem.ruleset.booking.sensor.ListenForExternalOrdersRule;
import org.greencloud.strategyinjection.agentsystem.ruleset.booking.service.CompareRestaurantOffersRule;
import org.greencloud.strategyinjection.agentsystem.ruleset.booking.service.LookForRestaurantForClientOrderRule;
import org.greencloud.strategyinjection.agentsystem.ruleset.restaurant.initial.StartInitialRestaurantBehaviours;
import org.greencloud.strategyinjection.agentsystem.ruleset.restaurant.service.ListenForNewClientOrdersRule;
import org.greencloud.strategyinjection.agentsystem.ruleset.restaurant.service.ProcessNewClientOrdersRule;
import org.greencloud.strategyinjection.agentsystem.ruleset.restaurant.service.ProposeToBookingServiceRule;

/**
 * Default rule set applied in the restaurant testing system
 */
@SuppressWarnings("unchecked")
public class DefaultRestaurantRuleSet extends RuleSet {

	public DefaultRestaurantRuleSet() {
		super(DEFAULT_RULE_SET);
	}

	@Override
	protected List<AgentRule> initializeRules(RulesController<?, ?> rulesController) {
		return new ArrayList<>(switch (rulesController.getAgentProps().getAgentType()) {
			case "BOOKING" -> getBookingRules((RulesController<BookingProps, BookingNode>) rulesController);
			case "RESTAURANT" -> getRestaurantRules(
					(RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>>) rulesController);
			default -> new ArrayList<AgentRule>();
		});
	}

	protected List<AgentRule> getBookingRules(RulesController<BookingProps, BookingNode> rulesController) {
		return List.of(
				new StartInitialBookingBehaviours(rulesController),
				new SearchForRestaurantsRule(rulesController),
				new ListenForExternalOrdersRule(rulesController),
				new LookForRestaurantForClientOrderRule(rulesController),
				new CompareRestaurantOffersRule(rulesController)
		);
	}

	protected List<AgentRule> getRestaurantRules(
			RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> rulesController) {
		return List.of(
				new StartInitialRestaurantBehaviours(rulesController),
				new ListenForNewClientOrdersRule(rulesController, this),
				new ProcessNewClientOrdersRule(rulesController),
				new ProposeToBookingServiceRule(rulesController)
		);
	}

}
