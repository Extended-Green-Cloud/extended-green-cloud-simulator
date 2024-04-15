package org.greencloud.agentsystem.strategies.deault.rules.server.adaptation;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_ENABLING_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class ProcessServerEnablingRule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessServerEnablingRule.class);

	public ProcessServerEnablingRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SERVER_ENABLING_RULE,
				"request Server enabling",
				"initiates Server enabling procedure");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(ENABLE_SERVER_PROTOCOL)
				.withStringContent(ENABLE_SERVER_PROTOCOL)
				.withReceivers(agentProps.getOwnerRegionalManagerAgent())
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		logger.info("Server was successfully enabled in Regional Manager {}.", inform.getSender().getName());
		if (nonNull(facts.get(MESSAGE))) {
			agent.send(prepareInformReply(facts.get(MESSAGE)));
		}

		final ACLMessage confirmationMessage = MessageBuilder.builder((int) facts.get(RULE_SET_IDX), INFORM)
				.withMessageProtocol(ENABLE_SERVER_PROTOCOL)
				.withReceivers(agentProps.getOwnerRegionalManagerAgent())
				.build();

		agent.send(confirmationMessage);
		agentNode.enableServer();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		logger.info("Enabling server failed - Server {} does not exists in a given Regional Manager.",
				refuse.getSender().getName());
		agentProps.disable();
		agentProps.saveMonitoringData();

		if (nonNull(facts.get(MESSAGE))) {
			agent.send(prepareFailureReply(facts.get(MESSAGE)));
		}
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not occur
	}
}
