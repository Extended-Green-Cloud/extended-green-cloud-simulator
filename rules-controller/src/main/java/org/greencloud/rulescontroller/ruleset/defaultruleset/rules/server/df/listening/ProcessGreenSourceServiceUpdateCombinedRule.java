package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening;

import static org.greencloud.commons.enums.rules.RuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing.ProcessGreenSourceServiceUpdateConnectRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing.ProcessGreenSourceServiceUpdateDeactivateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing.ProcessGreenSourceServiceUpdateDisconnectRule;

public class ProcessGreenSourceServiceUpdateCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessGreenSourceServiceUpdateCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
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
