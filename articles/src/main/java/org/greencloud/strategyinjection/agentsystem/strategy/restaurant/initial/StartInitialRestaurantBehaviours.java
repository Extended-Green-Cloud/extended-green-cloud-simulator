package org.greencloud.strategyinjection.agentsystem.strategy.restaurant.initial;

import static org.greencloud.commons.enums.rules.RuleType.BASIC_LISTENER;

import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;

import com.gui.agents.AgentNode;

import jade.core.behaviours.Behaviour;

public class StartInitialRestaurantBehaviours
		extends AgentBehaviourRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	public StartInitialRestaurantBehaviours(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				ListenForMessages.create(agent, BASIC_LISTENER, controller, true)
		);
	}
}
