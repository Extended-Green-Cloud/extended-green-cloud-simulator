package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RMA_NEW_JOB_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForRMANewJobRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRMANewJobRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientJob.class, LISTEN_FOR_RMA_NEW_JOB_TEMPLATE, 20,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new RMA job request",
				"listening for new job sent by RMA");
	}

	@Override
	public AgentRule copy() {
		return new ListenForRMANewJobRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
