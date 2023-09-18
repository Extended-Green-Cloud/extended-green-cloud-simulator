package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.adaptation;

import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_CONNECTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
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

public class ProcessConnectGreenSourceRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessConnectGreenSourceRule.class);

	public ProcessConnectGreenSourceRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SERVER_CONNECTION_RULE,
				"process connecting new Server with Green Source",
				"rule creates a request which is sent to Server and aims to connect it with new Green Source");
	}

	@Override
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		return MessageBuilder.builder((int) facts.get(STRATEGY_IDX))
				.withPerformative(REQUEST)
				.withMessageProtocol(CONNECT_GREEN_SOURCE_PROTOCOL)
				.withStringContent(CONNECT_GREEN_SOURCE_PROTOCOL)
				.withReceivers(new AID(facts.get(AGENT), AID.ISGUID))
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final StrategyFacts facts) {
		logger.info("Green Source successfully connected with a Server {}.", inform.getSender().getName());
		agent.send(prepareInformReply(facts.get(MESSAGE)));

		if (nonNull(agentNode)) {
			agentNode.updateServerConnection(inform.getSender().getName().split("@")[0], true);
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final StrategyFacts facts) {
		logger.info("Connection failed - Server {} is already connected to the Green Source.",
				refuse.getSender().getName());
		agent.send(prepareFailureReply(facts.get(MESSAGE)));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final StrategyFacts facts) {
		// case does not occur
	}
}
