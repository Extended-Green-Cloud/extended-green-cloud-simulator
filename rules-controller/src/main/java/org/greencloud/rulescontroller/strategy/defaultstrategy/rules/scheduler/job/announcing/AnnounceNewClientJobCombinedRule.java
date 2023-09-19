package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static java.util.Objects.nonNull;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing.processing.ProcessNewClientJobAdjustTimeRule;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing.processing.ProcessNewClientJobAfterDeadlineRule;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing.processing.ProcessNewClientJobOriginalTimeRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;

import com.gui.agents.scheduler.SchedulerNode;

public class AnnounceNewClientJobCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	public AnnounceNewClientJobCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, AgentCombinedRuleType.EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE,
				"announcing job to CNAs",
				"combined rule executed when new client job is to be announced to CNAs");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewClientJobOriginalTimeRule(controller),
				new ProcessNewClientJobAdjustTimeRule(controller, strategy),
				new ProcessNewClientJobAfterDeadlineRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		return nonNull(job)
				&& agentProps.getClientJobs().containsKey(job)
				&& agentProps.getClientJobs().get(job).equals(CREATED);
	}
}
