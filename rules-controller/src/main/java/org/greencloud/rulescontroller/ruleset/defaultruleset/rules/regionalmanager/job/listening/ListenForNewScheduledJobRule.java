package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForNewScheduledJobRule extends AgentMessageListenerRule<RegionalManagerAgentProps, RegionalManagerNode> {

	public ListenForNewScheduledJobRule(final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientJob.class, LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE, 50,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new scheduled jobs",
				"rule run when Scheduler sends new job to RMA");
	}
}
