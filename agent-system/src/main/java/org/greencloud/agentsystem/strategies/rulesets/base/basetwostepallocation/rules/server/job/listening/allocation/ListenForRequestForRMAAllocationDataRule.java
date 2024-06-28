package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.allocation;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ALLOCATION_DATA_REQUEST_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ALLOCATION_DATA_REQUEST_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_ALLOCATION_DATA_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForRequestForRMAAllocationDataRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRequestForRMAAllocationDataRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, AllocatedJobs.class, LISTEN_FOR_ALLOCATION_DATA_REQUEST_TEMPLATE, 10,
				ALLOCATION_DATA_REQUEST_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ALLOCATION_DATA_REQUEST_RULE,
				"handles request for data needed for allocation",
				"rule run to handle requests send by RMA asking for data needed for jobs allocation");
	}

	@Override
	public AgentRule copy() {
		return new ListenForRequestForRMAAllocationDataRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
