package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_POWER_SHORTAGE_FINISH_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForPowerShortageFinishRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForPowerShortageFinishRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobInstanceIdentifier.class, LISTEN_FOR_POWER_SHORTAGE_FINISH_TEMPLATE, 20,
				LISTEN_FOR_POWER_SHORTAGE_FINISH_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE,
				"listen for finish of power shortage in Green Source",
				"rule listens for information that power shortage has finished in the Green Source");
	}

	@Override
	public AgentRule copy() {
		return new ListenForPowerShortageFinishRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
