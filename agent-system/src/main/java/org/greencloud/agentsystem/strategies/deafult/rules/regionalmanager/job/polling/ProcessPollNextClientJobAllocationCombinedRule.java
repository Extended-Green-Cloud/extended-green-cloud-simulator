package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.polling;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.polling.processing.ProcessPollNextClientJobAllocationRule;
import org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.polling.processing.ProcessPollNextClientJobNoServersRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ProcessPollNextClientJobAllocationCombinedRule
		extends AgentCombinedRule<RegionalManagerAgentProps, RMANode> {

	public ProcessPollNextClientJobAllocationCombinedRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE,
				"polling next client job from queue",
				"combined rule executed when new client job is to be polled from queue");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPollNextClientJobNoServersRule(controller),
				new ProcessPollNextClientJobAllocationRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobAllocationCombinedRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
