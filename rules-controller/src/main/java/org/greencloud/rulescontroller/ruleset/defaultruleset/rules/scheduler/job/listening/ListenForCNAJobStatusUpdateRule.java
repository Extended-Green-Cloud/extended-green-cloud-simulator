package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForCNAJobStatusUpdateRule extends AgentMessageListenerRule<SchedulerAgentProps, SchedulerNode> {

	public ListenForCNAJobStatusUpdateRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobWithStatus.class, LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE, 20,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for update regarding client job status",
				"rule run when Scheduler reads message with updated client job status");
	}

	@Override
	protected int selectRuleSetIdx(final RuleSetFacts facts) {
		final JobWithStatus jobUpdate = facts.get(MESSAGE_CONTENT);
		return agentProps.getRuleSetForJob().get(jobUpdate.getJobInstance().getJobId());
	}
}
