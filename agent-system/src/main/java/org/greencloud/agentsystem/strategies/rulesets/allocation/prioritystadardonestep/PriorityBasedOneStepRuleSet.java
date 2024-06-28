package org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.ResourceAllocationAlgorithmTypes.CREDIT_PRIORITY_ALLOCATION;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.centralmanager.job.allocation.PrepareServerResourcesDataRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.regionalmanager.job.listening.allocation.PrepareRMAAllocationDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.centralmanager.job.allocation.RequestServerPriceEstimationDataRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.regionalmanager.job.listening.allocation.RequestServersForPriceEstimationRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.server.job.listening.allocation.PrepareServerPriceEstimationDataRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when one-step jobs allocation is to be computed
 */
public class PriorityBasedOneStepRuleSet extends RuleSet {

	public PriorityBasedOneStepRuleSet() {
		super(CREDIT_PRIORITY_ALLOCATION, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PrepareServerResourcesDataRequestRule(null),
				new RequestServerPriceEstimationDataRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new PrepareRMAAllocationDataRule(null),
				new RequestServersForPriceEstimationRule(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new PrepareServerPriceEstimationDataRule(null)
		);
	}
}
