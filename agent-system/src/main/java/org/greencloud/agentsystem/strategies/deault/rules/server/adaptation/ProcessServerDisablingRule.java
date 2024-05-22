package org.greencloud.agentsystem.strategies.deault.rules.server.adaptation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_DISABLING_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISABLE_SERVER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;

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
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(DISABLE_SERVER_PROTOCOL)
				.withStringContent(DISABLE_SERVER_PROTOCOL)
				.withReceivers(agentProps.getOwnerRegionalManagerAgent())
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		logger.info("Server was successfully disabled in Regional Manager {}.", inform.getSender().getName());
		if (nonNull(facts.get(MESSAGE))) {
			agent.send(prepareInformReply(facts.get(MESSAGE)));
		}

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
		logger.info("Disabling server failed - Server {} does not exists in a given Regional Manager.",
				refuse.getSender().getName());
		agentProps.enable();
		agentProps.saveMonitoringData();

		if (nonNull(facts.get(MESSAGE))) {
			agent.send(prepareFailureReply(facts.get(MESSAGE)));
		}
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not occur
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerDisablingRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
