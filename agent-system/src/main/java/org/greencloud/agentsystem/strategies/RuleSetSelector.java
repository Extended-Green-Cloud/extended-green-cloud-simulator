package org.greencloud.agentsystem.strategies;

import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.DEADLINE_BASED_PRIORITY;
import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.DURATION_BASED_PRIORITY;
import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.ESTIMATED_DURATION_BASED_PRIORITY;
import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.ESTIMATED_DURATION_WITH_ERROR_BASED_PRIORITY;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.BUDGET_DEADLINE_BASED_ALLOCATION;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.CREDIT_PRIORITY_ALLOCATION;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.INTENT_BASED_ALLOCATION;
import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;
import static org.jrba.utils.rules.RuleSetConstructor.modifyRuleSetForName;

import java.security.InvalidParameterException;

import org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.BudgetDeadlineOneStepRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.IntentBasedOneStepRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardtwostep.IntentBasedTwoStepRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.PriorityBasedOneStepRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.base.baseonestepallocation.StrategyBaseOneStepRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.StrategyBaseTwoStepRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.DefaultCloudRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.priority.durationpriority.DurationBasedPrioritizationRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimeenhancedpriority.ExecutionTimeWithErrorPrioritizationRuleSet;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.ExecutionTimePrioritizationRuleSet;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Class with methods allowing to select rule set corresponding to the given strategy.
 */
public class RuleSetSelector {

	/**
	 * Method selects one of the predefined rule sets by the strategy name.
	 *
	 * @param resourceAllocationStrategy name of the resource allocation rule set
	 * @param prioritizationStrategy     name of the tasks' prioritization rule set
	 * @param allocationStepsNumber      number of allocation steps
	 * @return selected RuleSet
	 */
	public static RuleSet selectRuleSetByName(final String resourceAllocationStrategy,
			final String prioritizationStrategy, final int allocationStepsNumber) {
		final RuleSet selectedPrioritizationRuleSet =
				selectPrioritizationRuleSet(prioritizationStrategy, allocationStepsNumber);
		return selectResourceAllocationRuleSet(selectedPrioritizationRuleSet, resourceAllocationStrategy,
				allocationStepsNumber);
	}

	private static RuleSet selectPrioritizationRuleSet(final String prioritizationAlgorithm,
			final int allocationStepsNumber) {
		final RuleSet baseRuleSet = selectBaseResourceAllocationRuleSet(allocationStepsNumber);

		return switch (prioritizationAlgorithm) {
			case DEADLINE_BASED_PRIORITY, DEFAULT_RULE_SET -> baseRuleSet;
			case DURATION_BASED_PRIORITY -> modifyRuleSetForName(baseRuleSet, new DurationBasedPrioritizationRuleSet());
			case ESTIMATED_DURATION_BASED_PRIORITY ->
					modifyRuleSetForName(baseRuleSet, new ExecutionTimePrioritizationRuleSet());
			case ESTIMATED_DURATION_WITH_ERROR_BASED_PRIORITY -> modifyRuleSetForName(
					selectPrioritizationRuleSet(ESTIMATED_DURATION_BASED_PRIORITY, allocationStepsNumber),
					new ExecutionTimeWithErrorPrioritizationRuleSet());
			default -> throw new InvalidParameterException("Incorrect prioritization rule set name.");
		};
	}

	private static RuleSet selectResourceAllocationRuleSet(final RuleSet baseRuleSet,
			final String resourceAllocationAlgorithm, final int allocationStepsNumber) {
		if (allocationStepsNumber == 1) {
			return switch (resourceAllocationAlgorithm) {
				case DEFAULT_RULE_SET -> baseRuleSet;
				case INTENT_BASED_ALLOCATION -> modifyRuleSetForName(baseRuleSet, new IntentBasedOneStepRuleSet());
				case CREDIT_PRIORITY_ALLOCATION -> modifyRuleSetForName(baseRuleSet, new PriorityBasedOneStepRuleSet());
				case BUDGET_DEADLINE_BASED_ALLOCATION ->
						modifyRuleSetForName(baseRuleSet, new BudgetDeadlineOneStepRuleSet());
				default -> throw new InvalidParameterException("Incorrect allocation rule set name.");
			};
		} else {
			return switch (resourceAllocationAlgorithm) {
				case DEFAULT_RULE_SET -> baseRuleSet;
				case INTENT_BASED_ALLOCATION -> modifyRuleSetForName(baseRuleSet, new IntentBasedTwoStepRuleSet());
				default -> throw new InvalidParameterException("Incorrect allocation rule set name.");
			};
		}
	}

	private static RuleSet selectBaseResourceAllocationRuleSet(final int allocationStepsNumber) {
		return switch (allocationStepsNumber) {
			case 2 -> modifyRuleSetForName(new DefaultCloudRuleSet(), new StrategyBaseTwoStepRuleSet());
			case 1 -> modifyRuleSetForName(selectBaseResourceAllocationRuleSet(2), new StrategyBaseOneStepRuleSet());
			default -> new DefaultCloudRuleSet();
		};
	}
}
