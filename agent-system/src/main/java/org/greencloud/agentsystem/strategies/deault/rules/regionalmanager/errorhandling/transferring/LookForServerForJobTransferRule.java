package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.transferring;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_TRANSFER_REQUEST_RULE;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.RMA_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.temporal.ValueRange;
import java.util.Collection;
import java.util.Comparator;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForSingleMessage;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentCFPRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class LookForServerForJobTransferRule extends AgentCFPRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(LookForServerForJobTransferRule.class);
	private static final ValueRange MAX_POWER_DIFFERENCE = ValueRange.of(-10, 10);

	public LookForServerForJobTransferRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_TRANSFER_REQUEST_RULE,
				"looking for server for job transfer",
				"sending call for proposal to server with job transfer");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		return prepareCallForProposal(facts.get(JOB), facts.get(AGENTS), RMA_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final int weight1 = agentProps.getWeightsForServersMap().get(bestProposal.getSender());
		final int weight2 = agentProps.getWeightsForServersMap().get(newProposal.getSender());

		final Comparator<ServerData> comparator = (msg1, msg2) -> {
			final double powerDiff = (msg1.getPowerConsumption() * weight2) - (msg2.getPowerConsumption() * weight1);
			final double priceDiff = ((msg1.getPriceForJob() * 1 / weight1) - (msg2.getPriceForJob() * 1 / weight2));

			return MAX_POWER_DIFFERENCE.isValidIntValue((int) powerDiff) ? (int) priceDiff : (int) powerDiff;
		};

		return compareMessages(bestProposal, newProposal, ServerData.class, comparator);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {
		agent.send(
				prepareReply(proposalToReject, JobMapper.mapClientJobToJobInstanceId(facts.get(JOB)), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No responses were retrieved from servers for job transfer");
		respondWithFailureMessage(facts);
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No Servers available - informing server about transfer failure");
		respondWithFailureMessage(facts);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final AID chosenServer = bestProposal.getSender();
		final ServerData serverData = readMessageContent(bestProposal, ServerData.class);
		final JobInstanceIdentifier jobInstance = JobMapper.mapClientJobToJobInstanceId(facts.get(JOB));
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Chosen Server for the job {} transfer: {}", serverData.getJobId(), chosenServer.getName());

		final ACLMessage replyToChosenOffer = prepareAcceptJobOfferReply(bestProposal, jobInstance,
				POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL);

		facts.put(JOB, jobInstance);
		facts.put(AGENT, chosenServer);
		agent.addBehaviour(ListenForSingleMessage.create(agent, facts, LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE,
				controller));
		agent.send(replyToChosenOffer);
	}

	private void respondWithFailureMessage(final RuleSetFacts facts) {
		final ACLMessage response = prepareReply(facts.get(MESSAGE), NO_SERVER_AVAILABLE_CAUSE_MESSAGE, FAILURE);
		agent.send(response);
	}
}
