package org.greencloud.rulescontroller.rule;

import static java.util.Collections.singletonList;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_RULE;

import java.util.List;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.jeasy.rules.api.Rule;

/**
 * Interface representing common agent rule (is implemented by all types of agent rules
 */
public interface AgentRule extends Rule {

	String RULE_NAME = "basic agent rule";
	String RULE_DESCRIPTION = "default rule definition";
	String TYPE = BASIC_RULE;

	String getAgentType();

	AgentRuleType getAgentRuleType();

	String getRuleType();

	String getSubRuleType();

	RuleStepType getStepType();

	boolean isRuleStep();

	/**
	 * Method returns constructed rule
	 *
	 * @return constructed rule Object
	 */
	default List<AgentRule> getRules() {
		return singletonList(this);
	}

	/**
	 * Method performs default evaluation of rule conditions
	 *
	 * @param facts facts used in evaluation
	 * @return boolean indicating if conditions are met
	 */
	default boolean evaluateRule(RuleSetFacts facts) {
		return true;
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	default void executeRule(RuleSetFacts facts) {
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	default AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TYPE, RULE_NAME, RULE_DESCRIPTION);
	}

	/**
	 * Method connects agent rule with controller
	 *
	 * @param rulesController rules controller connected to the agent
	 */
	default void connectToController(final RulesController<?, ?> rulesController) {
	}
}
