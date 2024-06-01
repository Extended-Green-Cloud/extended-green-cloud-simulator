package org.greencloud.agentsystem.strategies.deafult.rules.server.events.shortagegreensource.transfer;

import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.TRANSFER_JOB_IN_GS_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapPowerJobToEnergyJob;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.facts.ProposalsFactsFactory.constructFactsForProposalsComparison;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SOURCES_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForRMA;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.mapper.FactsMapper.mapToRuleSetFacts;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForSingleMessage;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentCFPRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class TransferInGreenSourceRule extends AgentCFPRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(TransferInGreenSourceRule.class);

	public TransferInGreenSourceRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TRANSFER_JOB_IN_GS_RULE,
				"transfer job to another Green Source",
				"rule initiates a transfer request of job to another Green Source");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final ClientJob job = newJobInstances.getSecondInstance();
		final double estimatedEnergy = agentProps.estimatePowerForJob(job);

		return prepareCallForProposal(mapPowerJobToEnergyJob(job, estimatedEnergy), facts.get(AGENTS),
				SERVER_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final RuleSetFacts comparatorFacts =
				constructFactsForProposalsComparison(facts.get(RULE_SET_IDX), bestProposal, newProposal);
		controller.fire(comparatorFacts);

		return comparatorFacts.get(RESULT);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final ClientJob jobInstance = newJobInstances.getSecondInstance();
		agent.send(prepareReply(proposalToReject, mapToJobInstanceId(jobInstance), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final RuleSetFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final ClientJob jobInstance = newJobInstances.getSecondInstance();
		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No responses were retrieved for job transfer");
		handleTransferFailure(facts);
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final JobInstanceIdentifier jobInstance = mapClientJobToJobInstanceId(newJobInstances.getSecondInstance());

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} transfer has failed in green source.", jobInstance.getJobId());
		handleTransferFailure(facts);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final JobInstanceIdentifier jobInstance = mapClientJobToJobInstanceId(newJobInstances.getSecondInstance());

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Chosen Green Source for the job transfer: {}", bestProposal.getSender().getLocalName());

		final RuleSetFacts factsListener = mapToRuleSetFacts(facts);
		factsListener.put(JOB, newJobInstances.getSecondInstance());

		agent.addBehaviour(ListenForSingleMessage.create(agent, mapToRuleSetFacts(factsListener),
				LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE, controller));
		agent.send(prepareAcceptJobOfferReply(bestProposal, jobInstance, POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL));
	}

	private void handleTransferFailure(final RuleSetFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final JobInstanceIdentifier jobInstance = mapClientJobToJobInstanceId(newJobInstances.getSecondInstance());
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			final String conversationId = agentProps.updateServerStateAfterFailedJobTransferBetweenGreenSources(job);

			agent.send(prepareJobStatusMessageForRMA(job, conversationId, agentProps, facts.get(RULE_SET_IDX)));
			agent.send(prepareStringReply(facts.get(MESSAGE), NO_SOURCES_AVAILABLE_CAUSE_MESSAGE, REFUSE));
		}
	}

	@Override
	public AgentRule copy() {
		return new TransferInGreenSourceRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
