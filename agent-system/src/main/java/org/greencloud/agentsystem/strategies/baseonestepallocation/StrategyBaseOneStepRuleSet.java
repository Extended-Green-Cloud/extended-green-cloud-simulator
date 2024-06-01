package org.greencloud.agentsystem.strategies.baseonestepallocation;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.STRATEGY_BASE_ONE_STEP_ALLOCATION_RULE_SET;

import java.util.List;

import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.centralmanager.job.allocation.processing.ProcessRegionalManagerAllocationRule;
import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.initial.StartInitialRegionalManagerOneAllocationBehaviours;
import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.announcing.RequestSelectedServerForExecutionRule;
import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob.ProcessNewJobServerVerificationCombinedRule;
import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob.ProcessNewJobsServerAllocationRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Base strategy rule set when one-step allocation is to be applied
 */
public class StrategyBaseOneStepRuleSet extends RuleSet {

	public StrategyBaseOneStepRuleSet() {
		super(STRATEGY_BASE_ONE_STEP_ALLOCATION_RULE_SET, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), rmaRules()).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new ProcessRegionalManagerAllocationRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new StartInitialRegionalManagerOneAllocationBehaviours(null),
				new RequestSelectedServerForExecutionRule(null),
				new ProcessNewJobServerVerificationCombinedRule(null),
				new ProcessNewJobsServerAllocationRule(null)
		);
	}
}
