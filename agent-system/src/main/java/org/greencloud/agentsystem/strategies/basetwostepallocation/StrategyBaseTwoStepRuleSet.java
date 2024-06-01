package org.greencloud.agentsystem.strategies.basetwostepallocation;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.STRATEGY_BASE_TWO_STEP_ALLOCATION_RULE_SET;

import java.util.List;

import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.centralmanager.job.allocation.processing.ProcessNotAllocatedJobsRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.centralmanager.job.allocation.processing.ProcessRegionalManagerFirstAllocationRuleRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.centralmanager.job.allocation.processing.ProcessRegionalManagerInitiateAllocationRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.centralmanager.job.announcing.RequestRMAForJobExecutionRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.initial.StartInitialRegionalManagerTwoAllocationBehaviours;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.allocation.processing.ProcessServerSecondAllocationRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.announcing.RequestServerForJobExecutionRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.allocation.ListenForRequestForAllocationDataRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.allocation.ProcessRequestForAllocationDataRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.ListenForNewJobsAllocationRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.ProcessNewJobVerificationCombinedRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.ProcessNewJobsAllocationRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.initial.StartInitialServerTwoAllocationBehaviours;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.job.listening.allocation.ListenForRequestForRMAAllocationDataRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.job.listening.allocation.ProcessRMARequestForAllocationData;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.job.listening.newjob.ListenForRMANewJobsAllocationRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.job.listening.newjob.ProcessRMANewJobVerificationCombinedRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.job.listening.newjob.ProcessRMANewJobsAllocationCombinedRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Base strategy rule set when two-step allocation is to be applied
 */
public class StrategyBaseTwoStepRuleSet extends RuleSet {

	public StrategyBaseTwoStepRuleSet() {
		super(STRATEGY_BASE_TWO_STEP_ALLOCATION_RULE_SET, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new ProcessRegionalManagerInitiateAllocationRule(null),
				new ProcessRegionalManagerFirstAllocationRuleRule(null),
				new ProcessNotAllocatedJobsRule(null),
				new RequestRMAForJobExecutionRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new StartInitialRegionalManagerTwoAllocationBehaviours(null),
				new ProcessServerSecondAllocationRule(null),
				new RequestServerForJobExecutionRule(null),
				new ListenForRequestForAllocationDataRule(null, this),
				new ProcessRequestForAllocationDataRule(null),
				new ListenForNewJobsAllocationRule(null, this),
				new ProcessNewJobsAllocationRule(null),
				new ProcessNewJobVerificationCombinedRule(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new StartInitialServerTwoAllocationBehaviours(null),
				new ListenForRMANewJobsAllocationRule(null, this),
				new ProcessRMANewJobsAllocationCombinedRule(null),
				new ProcessRMANewJobVerificationCombinedRule(null),
				new ListenForRequestForRMAAllocationDataRule(null, this),
				new ProcessRMARequestForAllocationData(null)
		);
	}
}
