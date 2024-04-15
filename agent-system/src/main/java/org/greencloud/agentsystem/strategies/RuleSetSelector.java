package org.greencloud.agentsystem.strategies;

import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;

import java.security.InvalidParameterException;

import org.greencloud.agentsystem.strategies.deault.DefaultCloudRuleSet;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Class with methods allowing to select rule set corresponding to the given strategy.
 */
public class RuleSetSelector {

	/**
	 * Method selects one of the predefined rule sets by the strategy name.
	 *
	 * @param ruleSetName name of the rule set to be selected
	 * @return selected RuleSet
	 */
	public static RuleSet selectRuleSetByName(final String ruleSetName) {
		return switch (ruleSetName) {
			case DEFAULT_RULE_SET -> new DefaultCloudRuleSet();
			default -> throw new InvalidParameterException("Incorrect rule set name");
		};
	}
}
