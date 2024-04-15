package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.announcing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.announcing.processing.ProcessNewClientJobAdjustTimeRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.announcing.processing.ProcessNewClientJobAfterDeadlineRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.announcing.processing.ProcessNewClientJobOriginalTimeRule;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class AnnounceNewClientJobCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	public AnnounceNewClientJobCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE,
				"announcing job to RMAs",
				"combined rule executed when new client job is to be announced to RMAs");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewClientJobOriginalTimeRule(controller),
				new ProcessNewClientJobAdjustTimeRule(controller, ruleSet),
				new ProcessNewClientJobAfterDeadlineRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return nonNull(job)
				&& agentProps.getClientJobs().containsKey(job)
				&& agentProps.getClientJobs().get(job).equals(CREATED);
	}
}
