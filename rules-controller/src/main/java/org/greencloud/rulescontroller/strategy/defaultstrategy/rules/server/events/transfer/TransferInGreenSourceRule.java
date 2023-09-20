package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.transfer;

import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.Math.signum;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_FOR_GS_IN_CNA_RULE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_IN_GS_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapPowerJobToEnergyJob;
import static org.greencloud.commons.mapper.JobMapper.mapToPowerShortageJob;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SOURCES_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.temporal.ValueRange;
import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.enums.job.JobExecutionStateEnum;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.behaviour.listen.ListenForSingleMessage;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class TransferInGreenSourceRule extends AgentCFPRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(TransferInGreenSourceRule.class);
	private static final ValueRange MAX_AVAILABLE_POWER_DIFFERENCE = ValueRange.of(-10, 10);

	public TransferInGreenSourceRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TRANSFER_JOB_IN_GS_RULE,
				"transfer job to another Green Source",
				"rule initiates a transfer request of job to another Green Source");
	}

	@Override
	protected ACLMessage createCFPMessage(final StrategyFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final ClientJob job = newJobInstances.getSecondInstance();
		final double estimatedEnergy = agentProps.estimateEnergyForJob(job);
		return prepareCallForProposal(mapPowerJobToEnergyJob(job, estimatedEnergy), facts.get(AGENTS),
				SERVER_JOB_CFP_PROTOCOL, facts.get(STRATEGY_IDX));
	}

	@Override
	protected int compareProposals(final ACLMessage bestProposal, final ACLMessage newProposal) {
		if (!agentProps.getWeightsForGreenSourcesMap().containsKey(bestProposal.getSender()) ||
				agentProps.getWeightsForGreenSourcesMap().containsKey(newProposal.getSender())) {
			return 0;
		}

		final int weight1 = agentProps.getWeightsForGreenSourcesMap().get(bestProposal.getSender());
		final int weight2 = agentProps.getWeightsForGreenSourcesMap().get(newProposal.getSender());

		final Comparator<GreenSourceData> comparator = (msg1, msg2) -> {
			double powerDiff = msg1.getAvailablePowerInTime() * weight2 - msg2.getAvailablePowerInTime() * weight1;
			double errorDiff = (msg1.getPowerPredictionError() - msg2.getPowerPredictionError());
			int priceDiff = (int) (msg1.getPriceForEnergySupply() - msg2.getPriceForEnergySupply());

			return (int) (errorDiff != 0 ? signum(errorDiff) :
					MAX_AVAILABLE_POWER_DIFFERENCE.isValidValue((long) powerDiff) ? priceDiff : signum(powerDiff));
		};

		return compareMessages(bestProposal, newProposal, GreenSourceData.class, comparator);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final StrategyFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final ClientJob jobInstance = newJobInstances.getSecondInstance();
		agent.send(prepareReply(proposalToReject, jobInstance, REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final StrategyFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final ClientJob jobInstance = newJobInstances.getSecondInstance();
		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("No responses were retrieved for job transfer");
		handleTransferFailure(facts);
	}

	@Override
	protected void handleNoProposals(final StrategyFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final JobInstanceIdentifier jobInstance = JobMapper.mapClientJobToJobInstanceId(
				newJobInstances.getSecondInstance());
		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Job {} transfer has failed in green source. Passing transfer request to Cloud Network",
				jobInstance.getJobId());
		final Instant shortageStart = facts.get(EVENT_TIME);
		final JobPowerShortageTransfer job = mapToPowerShortageJob(jobInstance, shortageStart);

		final StrategyFacts cnaTransferFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		cnaTransferFacts.put(JOB, job);
		cnaTransferFacts.put(JOB_ID, jobInstance);
		cnaTransferFacts.put(MESSAGE, facts.get(MESSAGE));

		agent.addBehaviour(
				InitiateRequest.create(agent, cnaTransferFacts, TRANSFER_JOB_FOR_GS_IN_CNA_RULE, controller));
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final StrategyFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final JobInstanceIdentifier jobInstance = JobMapper.mapClientJobToJobInstanceId(
				newJobInstances.getSecondInstance());
		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Chosen Green Source for the job {} transfer: {}", jobInstance.getJobId(),
				bestProposal.getSender().getLocalName());

		final StrategyFacts factsListener = FactsMapper.mapToStrategyFacts(facts);
		factsListener.put(JOB, newJobInstances.getSecondInstance());

		agent.addBehaviour(
				ListenForSingleMessage.create(agent, FactsMapper.mapToStrategyFacts(factsListener),
						LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE, controller));
		agent.send(prepareAcceptJobOfferReply(bestProposal, jobInstance, POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL));
	}

	private void handleTransferFailure(final StrategyFacts facts) {
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final JobInstanceIdentifier jobInstance = JobMapper.mapClientJobToJobInstanceId(
				newJobInstances.getSecondInstance());

		MDC.put(MDC_JOB_ID, newJobInstances.getSecondInstance().getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			final Triple<JobExecutionStateEnum, String, String> stateFields = getFieldsForJobState(job,
					newJobInstances, jobInstance);
			final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info(stateFields.getMiddle(), job.getJobId());
			agentProps.getServerJobs().replace(job, stateFields.getLeft().getStatus(hasStarted));

			if (hasStarted) {
				agent.send(prepareJobStatusMessageForCNA(JobMapper.mapClientJobToJobInstanceId(job),
						stateFields.getRight(), agentProps,
						facts.get(STRATEGY_IDX)));
			}
			agentProps.updateGUI();
			agent.send(prepareStringReply(facts.get(MESSAGE), NO_SOURCES_AVAILABLE_CAUSE_MESSAGE, REFUSE));
		}
	}

	private Triple<JobExecutionStateEnum, String, String> getFieldsForJobState(final ClientJob job,
			final JobDivided<ClientJob> newJobInstances, final JobInstanceIdentifier jobInstance) {
		final HardwareResources availableResources = agentProps.getAvailableResources(
				newJobInstances.getSecondInstance(), jobInstance, null);

		return !availableResources.areSufficient(job.getEstimatedResources()) ?
				new ImmutableTriple<>(EXECUTING_ON_HOLD_SOURCE,
						"There is not enough resources to process the job {} with back up power. Putting job on hold",
						ON_HOLD_JOB_ID) :
				new ImmutableTriple<>(EXECUTING_ON_BACK_UP, "Putting the job {} on back up power",
						BACK_UP_POWER_JOB_ID);
	}
}
