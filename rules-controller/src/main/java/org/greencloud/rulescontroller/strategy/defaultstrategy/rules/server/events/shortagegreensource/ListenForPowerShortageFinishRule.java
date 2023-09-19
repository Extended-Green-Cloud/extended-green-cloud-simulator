package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.shortagegreensource;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_POWER_SHORTAGE_FINISH_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import com.gui.agents.server.ServerNode;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

public class ListenForPowerShortageFinishRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForPowerShortageFinishRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, JobInstanceIdentifier.class, LISTEN_FOR_POWER_SHORTAGE_FINISH_TEMPLATE, 20,
				LISTEN_FOR_POWER_SHORTAGE_FINISH_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE,
				"listen for finish of power shortage in Green Source",
				"rule listens for information that power shortage has finished in the Green Source");
	}
}
