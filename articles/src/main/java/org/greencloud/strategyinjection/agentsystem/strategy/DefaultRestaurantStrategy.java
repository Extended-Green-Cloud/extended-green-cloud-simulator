package org.greencloud.strategyinjection.agentsystem.strategy;

import static org.greencloud.commons.enums.strategy.StrategyType.DEFAULT_STRATEGY;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.strategy.Strategy;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.greencloud.strategyinjection.agentsystem.strategy.booking.df.SearchForRestaurantsRule;
import org.greencloud.strategyinjection.agentsystem.strategy.booking.initial.StartInitialBookingBehaviours;
import org.greencloud.strategyinjection.agentsystem.strategy.booking.sensor.ListenForExternalOrdersRule;
import org.greencloud.strategyinjection.agentsystem.strategy.booking.service.CompareRestaurantOffersRule;
import org.greencloud.strategyinjection.agentsystem.strategy.booking.service.LookForRestaurantForClientOrderRule;
import org.greencloud.strategyinjection.agentsystem.strategy.restaurant.initial.StartInitialRestaurantBehaviours;
import org.greencloud.strategyinjection.agentsystem.strategy.restaurant.service.ListenForNewClientOrdersRule;
import org.greencloud.strategyinjection.agentsystem.strategy.restaurant.service.ProcessNewClientOrdersRule;
import org.greencloud.strategyinjection.agentsystem.strategy.restaurant.service.ProposeToBookingServiceRule;

import com.gui.agents.AgentNode;

/**
 * Default strategy applied in the restaurant testing system
 */
@SuppressWarnings("unchecked")
public class DefaultRestaurantStrategy extends Strategy {

	public DefaultRestaurantStrategy() {
		super(DEFAULT_STRATEGY.name());
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
