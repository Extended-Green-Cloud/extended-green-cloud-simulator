package org.greencloud.agentsystem.strategies.deafult.rules.server.df.listening.processing;

import static java.util.Collections.singletonList;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLE_CONNECT_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
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

public class ProcessGreenSourceServiceUpdateConnectRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessGreenSourceServiceUpdateConnectRule.class);

	public ProcessGreenSourceServiceUpdateConnectRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE,
				GREEN_SOURCE_STATUS_CHANGE_HANDLE_CONNECT_RULE,
				"handle Green Source connection",
				"updating connection state between server and green source");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getProtocol().equals(CONNECT_GREEN_SOURCE_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final AID greenSource = message.getSender();

		if (agentProps.getOwnedGreenSources().containsKey(greenSource)) {
			logger.info("Green Source {} is already connected to the given server.", greenSource.getName());
			agent.send(prepareRefuseReply(message));
		} else {
			logger.info("Connecting Green Source {} to the server", greenSource.getName());
			agentProps.connectNewGreenSourcesToServer(singletonList(greenSource));
			agent.send(prepareInformReply(message));
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessGreenSourceServiceUpdateConnectRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
