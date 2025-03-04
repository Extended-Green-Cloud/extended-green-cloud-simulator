package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLE_DEACTIVATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DEACTIVATE_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessGreenSourceServiceUpdateDeactivateRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessGreenSourceServiceUpdateDeactivateRule.class);

	public ProcessGreenSourceServiceUpdateDeactivateRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE,
				GREEN_SOURCE_STATUS_CHANGE_HANDLE_DEACTIVATE_RULE,
				"handle Green Source deactivation",
				"updating connection state between server and green source");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getProtocol().equals(DEACTIVATE_GREEN_SOURCE_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final AID greenSource = message.getSender();

		if (!agentProps.getOwnedGreenSources().containsKey(greenSource)) {
			logger.info("Green Source {} was not found in server connections", greenSource.getName());
			agent.send(prepareRefuseReply(message));
		} else {
			logger.info("Changing status of Green Source {} to inactive", greenSource.getName());
			agentProps.getOwnedGreenSources().replace(greenSource, false);
			agent.send(prepareInformReply(message));
		}
	}
}
