package org.greencloud.agentsystem.strategies.deafult.rules.server.df.listening.processing;

import static java.lang.Boolean.TRUE;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLE_DISCONNECT_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISCONNECT_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessGreenSourceServiceUpdateDisconnectRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessGreenSourceServiceUpdateDisconnectRule.class);

	public ProcessGreenSourceServiceUpdateDisconnectRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE,
				GREEN_SOURCE_STATUS_CHANGE_HANDLE_DISCONNECT_RULE,
				"handle Green Source disconnection",
				"updating connection state between server and green source");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getProtocol().equals(DISCONNECT_GREEN_SOURCE_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final AID greenSource = message.getSender();

		if (!agentProps.getOwnedGreenSources().containsKey(greenSource)) {
			logger.info("Green Source {} was not found in server connections", greenSource.getName());
			agent.send(prepareRefuseReply(message));
		} else if (TRUE.equals(agentProps.getOwnedGreenSources().get(greenSource)) ||
				isGreenSourceExecutingJobs(greenSource)) {
			logger.info("Green Source {} is still active thus cannot be disconnected.", greenSource.getName());
			agent.send(prepareRefuseReply(message));
		} else {
			logger.info("Disconnecting Green Source {} from given server", greenSource.getName());
			agentProps.getOwnedGreenSources().remove(greenSource);
			agent.send(prepareInformReply(message));
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessGreenSourceServiceUpdateDisconnectRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}

	private boolean isGreenSourceExecutingJobs(final AID greenSource) {
		return agentProps.getGreenSourceForJobMap().values().stream().anyMatch(agent -> agent.equals(greenSource));
	}
}
