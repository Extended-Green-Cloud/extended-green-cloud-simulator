package org.greencloud.rulescontroller.rule.simple;

import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BEHAVIOUR;

import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.core.behaviours.Behaviour;

/**
 * Abstract class defining structure of a rule which adds to the agent strategy-specific behaviours
 */
public abstract class AgentBehaviourRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentBehaviourRule(final RulesController<T, E> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	protected abstract Set<Behaviour> initializeBehaviours();

	@Override
	public void executeRule(final StrategyFacts facts) {
		final Set<Behaviour> behaviours = initializeBehaviours();
		behaviours.forEach(agent::addBehaviour);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(INITIALIZE_BEHAVIOURS_RULE,
				"initialize agent behaviours",
				"when strategy is selected and agent is set-up, it adds set of default behaviours");
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return BEHAVIOUR;
	}
}
