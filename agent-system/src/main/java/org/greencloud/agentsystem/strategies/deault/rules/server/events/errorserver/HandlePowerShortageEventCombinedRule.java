package org.greencloud.agentsystem.strategies.deault.rules.server.events.errorserver;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

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
