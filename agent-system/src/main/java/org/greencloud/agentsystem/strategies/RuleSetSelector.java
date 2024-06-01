package org.greencloud.agentsystem.strategies;

import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.EXECUTION_TIME_PRIORITIZATION_RULE_SET;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.INTENT_BASED_ONE_STEP_ALLOCATION_RULE_SET;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.STRATEGY_BASE_ONE_STEP_ALLOCATION_RULE_SET;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.STRATEGY_BASE_TWO_STEP_ALLOCATION_RULE_SET;
import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;
import static org.jrba.utils.rules.RuleSetConstructor.modifyRuleSetForName;

import java.security.InvalidParameterException;

import org.greencloud.agentsystem.strategies.baseonestepallocation.StrategyBaseOneStepRuleSet;
import org.greencloud.agentsystem.strategies.basetwostepallocation.StrategyBaseTwoStepRuleSet;
import org.greencloud.agentsystem.strategies.deafult.DefaultCloudRuleSet;
import org.greencloud.agentsystem.strategies.executiontimebased.ExecutionTimeBasedPrioritizationRuleSet;
import org.greencloud.agentsystem.strategies.intentstandardonestep.IntentBasedOneStepRuleSet;
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
			case EXECUTION_TIME_PRIORITIZATION_RULE_SET -> modifyRuleSetForName(new DefaultCloudRuleSet(),
					new ExecutionTimeBasedPrioritizationRuleSet());
			case STRATEGY_BASE_TWO_STEP_ALLOCATION_RULE_SET -> modifyRuleSetForName(new DefaultCloudRuleSet(),
					new StrategyBaseTwoStepRuleSet());
			case STRATEGY_BASE_ONE_STEP_ALLOCATION_RULE_SET ->
					modifyRuleSetForName(selectRuleSetByName(STRATEGY_BASE_TWO_STEP_ALLOCATION_RULE_SET),
							new StrategyBaseOneStepRuleSet());
			case INTENT_BASED_ONE_STEP_ALLOCATION_RULE_SET ->
					modifyRuleSetForName(selectRuleSetByName(STRATEGY_BASE_ONE_STEP_ALLOCATION_RULE_SET),
							new IntentBasedOneStepRuleSet());
			default -> throw new InvalidParameterException("Incorrect rule set name");
		};
	}
}
