package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForNewScheduledJobRule extends AgentMessageListenerRule<RegionalManagerAgentProps, RMANode> {

	public ListenForNewScheduledJobRule(final RulesController<RegionalManagerAgentProps, RMANode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientJob.class, LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE, 50,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new scheduled jobs",
				"rule run when CMA sends new job to RMA");
	}

	@Override
	public AgentRule copy() {
		return new ListenForNewScheduledJobRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
