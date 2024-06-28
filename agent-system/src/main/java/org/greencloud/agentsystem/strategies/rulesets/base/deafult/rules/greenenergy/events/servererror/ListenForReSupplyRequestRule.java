package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.servererror;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_RE_SUPPLY_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_RE_SUPPLY_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_POWER_RE_SUPPLY_REQUEST;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

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

	@Override
	public AgentRule copy() {
		return new ListenForReSupplyRequestRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
