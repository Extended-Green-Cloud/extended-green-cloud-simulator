package org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardtwostep;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.INTENT_BASED_ALLOCATION;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.server.listening.allocation.PrepareServerAllocationDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardtwostep.rules.centralmanager.job.allocation.PrepareLeastConnectionsDataRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardtwostep.rules.regionalmanager.job.listening.allocation.PrepareRMALeastConnectionAllocationDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardtwostep.rules.regionalmanager.job.listening.allocation.RequestServersForJobsEstimationRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when one-step jobs allocation is to be computed
 */
public class IntentBasedTwoStepRuleSet extends RuleSet {

	public IntentBasedTwoStepRuleSet() {
		super(INTENT_BASED_ALLOCATION, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PrepareLeastConnectionsDataRequestRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new PrepareRMALeastConnectionAllocationDataRule(null),
				new RequestServersForJobsEstimationRule(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new PrepareServerAllocationDataRule(null)
		);
	}
}
