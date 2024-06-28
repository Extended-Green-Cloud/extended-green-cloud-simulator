package org.greencloud.agentsystem.strategies.rulesets.priority.executiontimeenhancedpriority;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.ESTIMATED_DURATION_WITH_ERROR_BASED_PRIORITY;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimeenhancedpriority.rules.centralmanager.job.priority.ComputePriorityEnhancedExecutionTimeRule;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimeenhancedpriority.rules.regionalmanager.job.priority.RequestJobEnhancedExecutionTimeRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when jobs are to be prioritized by their estimated execution time
 */
public class ExecutionTimeWithErrorPrioritizationRuleSet extends RuleSet {

	public ExecutionTimeWithErrorPrioritizationRuleSet() {
		super(ESTIMATED_DURATION_WITH_ERROR_BASED_PRIORITY, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), rmaRules()).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new ComputePriorityEnhancedExecutionTimeRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new RequestJobEnhancedExecutionTimeRule(null)
		);
	}
}
