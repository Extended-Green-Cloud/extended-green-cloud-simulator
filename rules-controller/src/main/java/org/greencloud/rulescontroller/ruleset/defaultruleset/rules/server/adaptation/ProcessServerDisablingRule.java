package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_DISABLING_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISABLE_SERVER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
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

public class ProcessServerDisablingRule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessServerDisablingRule.class);

	public ProcessServerDisablingRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SERVER_DISABLING_RULE,
				"request Server disabling",
				"initiates Server disabling procedure");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX))
				.withPerformative(REQUEST)
				.withMessageProtocol(DISABLE_SERVER_PROTOCOL)
				.withStringContent(DISABLE_SERVER_PROTOCOL)
				.withReceivers(agentProps.getOwnerCloudNetworkAgent())
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		logger.info("Server was successfully disabled in Cloud Network {}.", inform.getSender().getName());
		agent.send(prepareInformReply(facts.get(MESSAGE)));

		if (agentProps.getServerJobs().size() > 0) {
			logger.info("Server will finish executing {} planned jobs before being fully disabled.",
					agentProps.getServerJobs().size());
			return;
		}
		logger.info("Server completed all planned jobs and is fully disabled.");
		agentNode.disableServer();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		logger.info("Disabling server failed - Server {} does not exists in a given Cloud Network.",
				refuse.getSender().getName());
		agentProps.enable();
		agentProps.saveMonitoringData();
		agent.send(prepareFailureReply(facts.get(MESSAGE)));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not occur
	}
}
