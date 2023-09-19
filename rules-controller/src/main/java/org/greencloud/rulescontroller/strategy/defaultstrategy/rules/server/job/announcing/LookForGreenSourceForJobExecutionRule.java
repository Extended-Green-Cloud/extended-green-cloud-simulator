package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.announcing;

import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.Math.signum;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapPowerJobToEnergyJob;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.temporal.ValueRange;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.server.ServerNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class LookForGreenSourceForJobExecutionRule extends AgentCFPRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(LookForGreenSourceForJobExecutionRule.class);
	private static final ValueRange MAX_AVAILABLE_POWER_DIFFERENCE = ValueRange.of(-10, 10);

	public LookForGreenSourceForJobExecutionRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"look for Green Source",
				"rule looks for Green Source that will provide power for job execution");
	}

	@Override
	protected ACLMessage createCFPMessage(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final List<AID> greenSources = agentProps.getOwnedActiveGreenSources().stream().toList();
		final double estimatedEnergy = agentProps.estimateEnergyForJob(job);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Energy estimated for executing job {} is {}.", job.getJobId(), estimatedEnergy);

		final EnergyJob energyJob = mapPowerJobToEnergyJob(job, estimatedEnergy);
		return prepareCallForProposal(energyJob, greenSources, SERVER_JOB_CFP_PROTOCOL, facts.get(STRATEGY_IDX));
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
		final ClientJob job = facts.get(JOB);
		agent.send(prepareReply(proposalToReject, JobMapper.mapClientJobToJobInstanceId(job), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("No responses were retrieved from green sources");
		refuseToExecuteJob(facts);
	}

	@Override
	protected void handleNoProposals(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("No Green Sources available - sending refuse message to Cloud Network Agent");
		refuseToExecuteJob(facts);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final HardwareResources serverResources = agentProps.getAvailableResources(job, null, null);

		agentProps.stoppedJobProcessing();

		if (!serverResources.areSufficient(job.getEstimatedResources())) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("Not enough resources - sending refuse message to Cloud Network Agent");
			refuseToExecuteJob(facts);
		} else {
			handleSelectedProposal(bestProposal, facts);
		}
	}

	private void handleSelectedProposal(final ACLMessage bestProposal, final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		if (agentProps.getServerJobs().containsKey(job)) {
			final AID greenSource = bestProposal.getSender();
			final String jobId = job.getJobId();

			MDC.put(MDC_JOB_ID, jobId);
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("Chosen Green Source for the job with id {} : {}. "
					+ "Sending job volunteering offer to Cloud Network Agent", jobId, greenSource.getLocalName());

			agentProps.getGreenSourceForJobMap().put(jobId, greenSource);

			facts.put(ORIGINAL_MESSAGE, facts.get(MESSAGE));
			facts.put(MESSAGE, bestProposal);
			agent.addBehaviour(
					InitiateProposal.create(agent, FactsMapper.mapToStrategyFacts(facts), PROPOSE_TO_EXECUTE_JOB_RULE,
							controller));
		} else {
			agent.send(prepareReply(bestProposal, JobMapper.mapClientJobToJobInstanceId(job), REJECT_PROPOSAL));
			refuseToExecuteJob(facts);
		}
	}

	private void refuseToExecuteJob(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		agentProps.removeJob(job);
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
