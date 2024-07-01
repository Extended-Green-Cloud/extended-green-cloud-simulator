package org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.INTENT_BASED_ALLOCATION;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.centralmanager.ParseServerResourcesRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.regionalmanager.PrepareServerResourcesRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.server.PrepareJobsExecutionEstimationRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.centralmanager.job.allocation.PrepareServerResourcesDataRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.centralmanager.job.allocation.RequestServerResourcesDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.regionalmanager.job.listening.allocation.PrepareRMAAllocationDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.regionalmanager.job.listening.allocation.RequestServersForJobsExecutionEstimationRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.server.listening.allocation.PrepareServerAllocationDataRule;
import org.greencloud.commons.domain.agent.ServerJobsEstimation;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when one-step jobs allocation is to be computed
 */
public class IntentBasedOneStepRuleSet extends RuleSet {

	public IntentBasedOneStepRuleSet() {
		super(INTENT_BASED_ALLOCATION, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PrepareServerResourcesDataRequestRule(null),
				new ParseServerResourcesRule(null),
				new RequestServerResourcesDataRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new PrepareRMAAllocationDataRule(null),
				new RequestServersForJobsExecutionEstimationRule(null),
				new PrepareServerResourcesRule<>(null, ServerJobsEstimation.class)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new PrepareServerAllocationDataRule(null),
				new PrepareJobsExecutionEstimationRule(null)
		);
	}
}
