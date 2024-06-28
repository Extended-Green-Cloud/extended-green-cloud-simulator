package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_GREEN_SOURCE_POWER_SUPPLY_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForUpdatesFromGreenSourceRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForUpdatesFromGreenSourceRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobInstanceIdentifier.class, LISTEN_FOR_GREEN_SOURCE_POWER_SUPPLY_UPDATE_TEMPLATE,
				20, JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for updates regarding job execution",
				"listening for messages received from Green Source informing about changes in power supply");
	}

	@Override
	public AgentRule copy() {
		return new ListenForUpdatesFromGreenSourceRule(controller, ruleSet);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
