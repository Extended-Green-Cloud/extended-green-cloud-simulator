package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_NEW_CLIENT_JOB_TEMPLATE;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForNewClientJobsRule extends AgentMessageListenerRule<SchedulerAgentProps, SchedulerNode> {

	public ListenForNewClientJobsRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientJob.class, LISTEN_FOR_NEW_CLIENT_JOB_TEMPLATE, 1,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new client jobs",
				"rule run when Scheduler reads new Client Job message");
	}
}
