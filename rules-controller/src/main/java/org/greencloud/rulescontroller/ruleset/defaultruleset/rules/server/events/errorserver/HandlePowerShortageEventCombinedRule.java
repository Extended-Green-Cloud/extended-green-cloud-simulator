package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.errorserver;

import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;

public class HandlePowerShortageEventCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public HandlePowerShortageEventCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE,
				"handle power shortage event",
				"rule handles different cases of power shortage event");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPowerShortageStartEventCombinedRule(controller).getRules().get(0),
				new ProcessPowerShortageFinishEventRule(controller)
		);
	}
}
