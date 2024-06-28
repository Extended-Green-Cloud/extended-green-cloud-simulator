package org.greencloud.agentsystem.strategies.rulesets.priority.durationpriority;

import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.DURATION_BASED_PRIORITY;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.priority.durationpriority.rules.centralmanager.job.priority.ComputeJobPriorityBasedOnDurationRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when jobs are to be prioritized by their duration
 */
public class DurationBasedPrioritizationRuleSet extends RuleSet {

	public DurationBasedPrioritizationRuleSet() {
		super(DURATION_BASED_PRIORITY, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return cmaRules();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new ComputeJobPriorityBasedOnDurationRule(null)
		);
	}
}
