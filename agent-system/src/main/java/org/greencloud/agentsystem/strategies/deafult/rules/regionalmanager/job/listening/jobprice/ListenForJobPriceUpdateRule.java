package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.listening.jobprice;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_ENERGY_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_PRICE_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForJobPriceUpdateRule extends AgentMessageListenerRule<RegionalManagerAgentProps, RMANode> {

	public ListenForJobPriceUpdateRule(final RulesController<RegionalManagerAgentProps, RMANode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobInstanceWithPrice.class, LISTEN_FOR_PRICE_UPDATE_TEMPLATE,
				10, JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_ENERGY_PRICE_RECEIVER_RULE,
				"listen for updates regarding job execution price",
				"listening for messages received from Server informing about job execution price");
	}

	@Override
	public AgentRule copy() {
		return new ListenForJobPriceUpdateRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
