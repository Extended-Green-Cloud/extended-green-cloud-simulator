package org.greencloud.agentsystem.strategies.deafult.rules.server.job.polling;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deafult.rules.server.job.polling.processing.ProcessPollNextClientJobForExecutionNoResourcesRule;
import org.greencloud.agentsystem.strategies.deafult.rules.server.job.polling.processing.ProcessPollNextClientJobForExecutionSuccessfullyRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ProcessPollNextClientJobForExecutionCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessPollNextClientJobForExecutionCombinedRule(
			final RulesController<ServerAgentProps, ServerNode> controller,
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
				new ProcessPollNextClientJobForExecutionNoResourcesRule(controller),
				new ProcessPollNextClientJobForExecutionSuccessfullyRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobForExecutionCombinedRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
