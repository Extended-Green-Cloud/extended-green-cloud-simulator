package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_RE_SUPPLY_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_RE_SUPPLY_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_POWER_RE_SUPPLY_REQUEST;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForReSupplyRequestRule extends AgentMessageListenerRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ListenForReSupplyRequestRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller, final RuleSet ruleSet) {
		super(controller, ruleSet, JobInstanceIdentifier.class, LISTEN_FOR_SERVER_POWER_RE_SUPPLY_REQUEST, 30,
				LISTEN_FOR_SERVER_RE_SUPPLY_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_SERVER_RE_SUPPLY_RULE,
				"listen for information about Server error",
				"listening for different types of information regarding possible Server errors");
	}
}
