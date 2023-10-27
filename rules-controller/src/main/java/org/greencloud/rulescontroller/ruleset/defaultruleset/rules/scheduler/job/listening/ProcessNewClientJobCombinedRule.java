package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.processing.ProcessNewClientJobAlreadyExistsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.processing.ProcessNewClientJobQueueLimitRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.processing.ProcessNewClientJobRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;

import com.gui.agents.scheduler.SchedulerNode;

public class ProcessNewClientJobCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	public ProcessNewClientJobCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handling new client jobs",
				"rule run when Scheduler processes new Client Job message");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewClientJobAlreadyExistsRule(controller),
				new ProcessNewClientJobQueueLimitRule(controller),
				new ProcessNewClientJobRule(controller)
		);
	}
}
