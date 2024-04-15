package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessNewScheduledJobNoServersRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessNewScheduledJobRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessNewScheduledJobCombinedRule
		extends AgentCombinedRule<RegionalManagerAgentProps, RegionalManagerNode> {
	public ProcessNewScheduledJobCombinedRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new scheduled jobs",
				"rule run when RMA processes new job received from CBA");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewScheduledJobNoServersRule(controller),
				new ProcessNewScheduledJobRule(controller)
		);
	}
}
