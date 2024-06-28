package org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.BUDGET_DEADLINE_BASED_ALLOCATION;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.rules.centralmanager.job.allocation.RequestServerJobExecutionDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.rules.regionalmanager.job.listening.allocation.RequestServersForJobExecutionEstimationRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.rules.server.job.listening.allocation.PrepareServerJobEstimationDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.centralmanager.job.allocation.PrepareServerResourcesDataRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.regionalmanager.job.listening.allocation.PrepareRMAAllocationDataRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when budget-deadline constrained jobs allocation is to be computed
 */
public class BudgetDeadlineOneStepRuleSet extends RuleSet {

	public BudgetDeadlineOneStepRuleSet() {
		super(BUDGET_DEADLINE_BASED_ALLOCATION, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PrepareServerResourcesDataRequestRule(null),
				new RequestServerJobExecutionDataRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new PrepareRMAAllocationDataRule(null),
				new RequestServersForJobExecutionEstimationRule(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new PrepareServerJobEstimationDataRule(null)
		);
	}
}
