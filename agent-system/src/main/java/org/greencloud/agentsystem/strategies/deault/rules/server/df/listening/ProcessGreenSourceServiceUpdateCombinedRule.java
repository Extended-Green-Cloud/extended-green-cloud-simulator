package org.greencloud.agentsystem.strategies.deault.rules.server.df.listening;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.server.df.listening.processing.ProcessGreenSourceServiceUpdateConnectRule;
import org.greencloud.agentsystem.strategies.deault.rules.server.df.listening.processing.ProcessGreenSourceServiceUpdateDeactivateRule;
import org.greencloud.agentsystem.strategies.deault.rules.server.df.listening.processing.ProcessGreenSourceServiceUpdateDisconnectRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessGreenSourceServiceUpdateCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessGreenSourceServiceUpdateCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE,
				"handler updates in green source connection state",
				"updating connection state between server and green source");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessGreenSourceServiceUpdateDeactivateRule(controller),
				new ProcessGreenSourceServiceUpdateDisconnectRule(controller),
				new ProcessGreenSourceServiceUpdateConnectRule(controller)
		);
	}

}
