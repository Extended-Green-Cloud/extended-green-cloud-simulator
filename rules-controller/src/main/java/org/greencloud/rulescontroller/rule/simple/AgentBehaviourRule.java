package org.greencloud.rulescontroller.rule.simple;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BEHAVIOUR;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.BehaviourRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import jade.core.behaviours.Behaviour;

/**
 * Abstract class defining structure of a rule which adds to the agent rule-set-specific behaviours
 */
public class AgentBehaviourRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<Serializable> expressionsBehaviours;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentBehaviourRule(final RulesController<T, E> controller) {
		super(controller);
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentBehaviourRule(final BehaviourRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getBehaviours())) {
			this.expressionsBehaviours = ruleRest.getBehaviours().stream()
					.map(behaviourExp -> MVEL.compileExpression(imports + " " + behaviourExp))
					.toList();
		}
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	protected Set<Behaviour> initializeBehaviours() {
		return new HashSet<>();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		if (nonNull(this.initialParameters)) {
			this.initialParameters.replace("facts", facts);
		}

		final Set<Behaviour> behaviours;

		if (isNull(expressionsBehaviours)) {
			behaviours = initializeBehaviours();
		} else {
			behaviours = this.expressionsBehaviours.stream()
					.map(exp -> (Behaviour) MVEL.executeExpression(exp, this.initialParameters))
					.collect(Collectors.toSet());
		}
		behaviours.forEach(agent::addBehaviour);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(INITIALIZE_BEHAVIOURS_RULE,
				"initialize agent behaviours",
				"when rule set is selected and agent is set-up, it adds set of default behaviours");
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return BEHAVIOUR;
	}
}
