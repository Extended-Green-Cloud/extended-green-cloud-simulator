package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.df.listening;

import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_CNA_RESOURCE_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import com.gui.agents.server.ServerNode;

public class ListenForCNAResourceInformationRequestRule extends
		AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForCNAResourceInformationRequestRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, LISTEN_FOR_CNA_RESOURCE_REQUEST_TEMPLATE, 1,
				"CNA_RESOURCE_REQUEST_HANDLER_RULE");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("CNA_RESOURCE_REQUEST_RULE",
				"listen for CNA request about server resources",
				"sends information about server resources to CNA");
	}
}

