package org.greencloud.strategyinjection.agentsystem.agents;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;
import static org.greencloud.commons.enums.strategy.StrategyType.DEFAULT_STRATEGY;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;

import com.gui.agents.AgentNode;

import jade.core.Agent;

/**
 * Agent storing common objects of system agents
 */
public class AbstractAgent<T extends AgentNode<E>, E extends AgentProps> extends Agent {

	protected RulesController<E, T> rulesController;
	protected E properties;
	protected T node;

	/**
	 * Method responsible for running initial custom behaviours prepared only for selected strategy
	 */
	public void runInitialBehavioursForStrategy() {
		final StrategyFacts facts = new StrategyFacts(rulesController.getLatestStrategy().get());
		facts.put(RULE_TYPE, INITIALIZE_BEHAVIOURS_RULE);
		rulesController.fire(facts);
	}

	/**
	 * Method sets up the rules controller
	 */
	public void setRulesController() {
		rulesController.setAgent(this, properties, node, DEFAULT_STRATEGY.name());
		runInitialBehavioursForStrategy();
	}
}
