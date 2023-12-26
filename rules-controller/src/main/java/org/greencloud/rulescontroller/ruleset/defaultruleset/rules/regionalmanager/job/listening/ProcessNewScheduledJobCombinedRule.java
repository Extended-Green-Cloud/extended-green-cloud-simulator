package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.processing.ProcessNewScheduledJobNoServersRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.processing.ProcessNewScheduledJobRule;

public class ProcessNewScheduledJobCombinedRule extends AgentCombinedRule<RegionalManagerAgentProps, RegionalManagerNode> {
	public ProcessNewScheduledJobCombinedRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
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
