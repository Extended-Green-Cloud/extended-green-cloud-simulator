package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ListenForCNAJobStatusUpdateRule extends AgentMessageListenerRule<SchedulerAgentProps, SchedulerNode> {

	public ListenForCNAJobStatusUpdateRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, JobWithStatus.class, LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE, 20,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for update regarding client job status",
				"rule run when Scheduler reads message with updated client job status");
	}

	@Override
	protected int selectStrategyIdx(final StrategyFacts facts) {
		final JobWithStatus jobUpdate = facts.get(MESSAGE_CONTENT);
		return agentProps.getStrategyForJob().get(jobUpdate.getJobInstance().getJobId());
	}
}
