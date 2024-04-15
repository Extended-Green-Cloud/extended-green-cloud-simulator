package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessNewClientJobAlreadyExistsRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessNewClientJobQueueLimitRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessNewClientJobRule;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessNewClientJobCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	public ProcessNewClientJobCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, EXECUTE_FIRST);
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
