package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling.processing.ProcessPollNextClientJobNoCloudAgentsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling.processing.ProcessPollNextClientJobSuccessfullyRule;
import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ProcessPollNextClientJobCombinedRule extends AgentCombinedRule<CentralManagerAgentProps, CMANode> {

	public ProcessPollNextClientJobCombinedRule(final RulesController<CentralManagerAgentProps, CMANode> controller,
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
				new ProcessPollNextClientJobNoCloudAgentsRule(controller),
				new ProcessPollNextClientJobSuccessfullyRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobCombinedRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
