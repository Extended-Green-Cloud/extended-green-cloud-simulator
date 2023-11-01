package org.greencloud.strategyinjection.agentsystem.agents;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_RULE_SET;
import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;

/**
 * Agent storing common objects of system agents
 */
public class AbstractAgent<T extends AgentNode<E>, E extends AgentProps> extends Agent {

	protected RulesController<E, T> rulesController;
	protected E properties;
	protected T node;

	/**
	 * Method responsible for running initial custom behaviours prepared only for selected rule set
	 */
	public void runInitialBehavioursForStrategy() {
		final RuleSetFacts facts = new RuleSetFacts(rulesController.getLatestRuleSet().get());
		facts.put(RULE_TYPE, INITIALIZE_BEHAVIOURS_RULE);
		rulesController.fire(facts);
	}

	/**
	 * Method sets up the rules controller
	 */
	public void setRulesController() {
		rulesController.setAgent(this, properties, node, DEFAULT_RULE_SET);
		runInitialBehavioursForStrategy();
	}
}
