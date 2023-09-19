package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.adaptation;

import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_DISCONNECTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISCONNECT_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessDisconnectingGreenSourceRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessDisconnectingGreenSourceRule.class);

	public ProcessDisconnectingGreenSourceRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SERVER_DISCONNECTION_RULE,
				"process disconnecting new Server with Green Source",
				"rule creates a request which is sent to Server and aims to disconnect it with new Green Source");
	}

	@Override
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		return MessageBuilder.builder((int) facts.get(STRATEGY_IDX))
				.withPerformative(REQUEST)
				.withMessageProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL)
				.withStringContent(DISCONNECT_GREEN_SOURCE_PROTOCOL)
				.withReceivers((AID) facts.get(AGENT))
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final StrategyFacts facts) {
		logger.info("Green Source was successfully disconnected from Server {}.", inform.getSender().getName());
		agentProps.getGreenSourceDisconnection().reset();
		agent.send(prepareInformReply(facts.get(MESSAGE)));

		if (nonNull(agentNode)) {
			agentNode.updateServerConnection(inform.getSender().getName().split("@")[0], false);
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final StrategyFacts facts) {
		logger.info("Disconnection failed - Server {} couldn't disconnect a Green Source.",
				refuse.getSender().getName());
		agentProps.getGreenSourceDisconnection().reset();
		agent.send(prepareFailureReply(facts.get(MESSAGE)));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final StrategyFacts facts) {
		// case does not occur
	}
}
