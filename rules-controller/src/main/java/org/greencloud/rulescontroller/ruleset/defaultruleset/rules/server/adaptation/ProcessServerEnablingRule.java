package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_ENABLING_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import com.gui.agents.server.ServerNode;

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
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX))
				.withPerformative(REQUEST)
				.withMessageProtocol(ENABLE_SERVER_PROTOCOL)
				.withStringContent(ENABLE_SERVER_PROTOCOL)
				.withReceivers(agentProps.getOwnerCloudNetworkAgent())
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		logger.info("Server was successfully enabled in Cloud Network {}.", inform.getSender().getName());
		if(nonNull(facts.get(MESSAGE))) {
			agent.send(prepareInformReply(facts.get(MESSAGE)));
		}

		final ACLMessage confirmationMessage = MessageBuilder.builder((int) facts.get(RULE_SET_IDX))
				.withPerformative(INFORM)
				.withMessageProtocol(ENABLE_SERVER_PROTOCOL)
				.withReceivers(agentProps.getOwnerCloudNetworkAgent())
				.build();

		agent.send(confirmationMessage);
		agentNode.enableServer();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		logger.info("Enabling server failed - Server {} does not exists in a given Cloud Network.",
				refuse.getSender().getName());
		agentProps.disable();
		agentProps.saveMonitoringData();

		if(nonNull(facts.get(MESSAGE))) {
			agent.send(prepareFailureReply(facts.get(MESSAGE)));
		}
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not occur
	}
}
