package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_RULE;

import java.util.List;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing.ProcessNewClientJobAdjustTimeRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing.ProcessNewClientJobAfterDeadlineRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing.ProcessNewClientJobOriginalTimeRule;

public class AnnounceNewClientJobCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	public AnnounceNewClientJobCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, AgentCombinedRuleType.EXECUTE_FIRST);
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
