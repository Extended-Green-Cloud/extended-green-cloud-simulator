package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.adaptation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_DEACTIVATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_DISCONNECTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DEACTIVATE_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessDeactivationOfGreenSourceRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessDeactivationOfGreenSourceRule.class);

	public ProcessDeactivationOfGreenSourceRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SERVER_DEACTIVATION_RULE,
				"process deactivation connection between Server and Green Source",
				"rule creates a request to deactive Server and Green Source connection");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(DEACTIVATE_GREEN_SOURCE_PROTOCOL)
				.withStringContent(DEACTIVATE_GREEN_SOURCE_PROTOCOL)
				.withReceivers(new AID(facts.get(AGENT), AID.ISGUID))
				.build();
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		logger.info("Green Source was successfully deactivated in a Server {}.", inform.getSender().getName());

		final long numberOfServerJobs = agentProps.getServerJobs().keySet().stream()
				.filter(job -> job.getServer().equals(inform.getSender()))
				.count();

		if (numberOfServerJobs > 0) {
			logger.info("There are {} Server jobs left. Green Source will wait for remaining jobs finish.",
					numberOfServerJobs);
			agentProps.getGreenSourceDisconnection().setServerToBeDisconnected(inform.getSender());
		} else {
			logger.info("There are no Server jobs left. Initiating Green Source disconnection");

			final RuleSetFacts disconnectFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			disconnectFacts.put(AGENT, inform.getSender());
			disconnectFacts.put(MESSAGE, agentProps.getGreenSourceDisconnection().getOriginalAdaptationMessage());

			agent.addBehaviour(
					InitiateRequest.create(agent, disconnectFacts, PROCESS_SERVER_DISCONNECTION_RULE, controller));
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		logger.info("Deactivation failed - Server {} is not connected with a given Green Source.",
				refuse.getSender().getName());
		agentProps.getGreenSourceDisconnection().setBeingDisconnected(false);
		agent.send(prepareFailureReply(agentProps.getGreenSourceDisconnection().getOriginalAdaptationMessage()));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not occur
	}
}
