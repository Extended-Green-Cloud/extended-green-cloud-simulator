package org.greencloud.agentsystem.strategies.intentstandardonestep;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.INTENT_BASED_ONE_STEP_ALLOCATION_RULE_SET;

import java.util.List;

import org.greencloud.agentsystem.strategies.intentstandardonestep.centralmanager.job.allocation.PrepareServerResourcesDataRequestRule;
import org.greencloud.agentsystem.strategies.intentstandardonestep.centralmanager.job.allocation.RequestServerResourcesDataRule;
import org.greencloud.agentsystem.strategies.intentstandardonestep.regionalmanager.PrepareRMAAllocationDataRule;
import org.greencloud.agentsystem.strategies.intentstandardonestep.regionalmanager.RequestServersForJobsExecutionEstimationRule;
import org.greencloud.agentsystem.strategies.intentstandardonestep.server.PrepareServerAllocationDataRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when one-step jobs allocation is to be computed
 */
public class IntentBasedOneStepRuleSet extends RuleSet {

	public IntentBasedOneStepRuleSet() {
		super(INTENT_BASED_ONE_STEP_ALLOCATION_RULE_SET, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PrepareServerResourcesDataRequestRule(null),
				new RequestServerResourcesDataRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new PrepareRMAAllocationDataRule(null),
				new RequestServersForJobsExecutionEstimationRule(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new PrepareServerAllocationDataRule(null)
		);
	}
}
