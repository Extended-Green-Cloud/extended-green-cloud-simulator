package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_NEW_JOB_TEMPLATE;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForServerNewJobRule extends AgentMessageListenerRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ListenForServerNewJobRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, EnergyJob.class, LISTEN_FOR_SERVER_NEW_JOB_TEMPLATE, 30,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new Server power supply request",
				"listening for new request for power supply coming from Server");
	}

	@Override
	public AgentRule copy() {
		return new ListenForServerNewJobRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
