package org.greencloud.rulescontroller.domain;

import org.greencloud.commons.enums.rules.RuleStepType;

/**
 * Class storing common properties which describe given rule
 *
 * @param ruleType        type of the rule
 * @param subType         secondary type used in combined rules
 * @param stepType        optional type of rule step
 * @param ruleName        name of the rule
 * @param ruleDescription description of the rule
 */
public record AgentRuleDescription(String ruleType, String subType, RuleStepType stepType, String ruleName,
								   String ruleDescription) {

	public AgentRuleDescription(final String ruleType, final String ruleName, final String ruleDescription) {
		this(ruleType, null, null, ruleName, ruleDescription);
	}

	public AgentRuleDescription(final String ruleType, final String subType, final String ruleName,
			final String ruleDescription) {
		this(ruleType, subType, null, ruleName, ruleDescription);
	}

	public AgentRuleDescription(final String ruleType, final RuleStepType stepType, final String ruleName,
			final String ruleDescription) {
		this(ruleType, null, stepType, ruleName, ruleDescription);
	}
}
