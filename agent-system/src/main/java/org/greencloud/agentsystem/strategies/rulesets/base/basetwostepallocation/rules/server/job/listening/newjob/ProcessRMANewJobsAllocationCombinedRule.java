package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.UNKNOWN;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.mapper.AgentDataMapper.mapToServerData;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobsWithRejection;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.allocation.AllocationUtils.verifyJobsForAllocation;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobVerification;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobConfirmationMessageForRMA;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessRMANewJobsAllocationCombinedRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMANewJobsAllocationCombinedRule.class);

	public ProcessRMANewJobsAllocationCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles RMA request for jobs execution",
				"handling message received from RMA requesting jobs execution");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage receivedMsg = facts.get(MESSAGE);
		final int index = facts.get(RULE_SET_IDX);

		verifyJobsForAllocation(
				facts,
				index,
				agent.getName(),
				performVerification(receivedMsg, index),
				passJobsExecutionDecision(receivedMsg, index, facts)
		);
	}

	private Function<ClientJobWithServer, RuleSetFacts> performVerification(final ACLMessage message, final int index) {
		return job -> {
			agentProps.addJob(mapToClientJob(job), index, PROCESSING);
			agentProps.takeJobIntoProcessing();

			final RuleSetFacts verifierFacts = constructFactsForJobVerification(index, job, message);
			controller.fire(verifierFacts);
			return verifierFacts;
		};
	}

	private BiConsumer<List<ClientJobWithServer>, List<ClientJobWithServer>> passJobsExecutionDecision(
			final ACLMessage message, final int index, final RuleSetFacts facts) {
		return (acceptedJobs, refusedJobs) -> {
			if (refusedJobs.isEmpty()) {
				agent.send(prepareReply(message, mapToAllocatedJobs(acceptedJobs), INFORM));
			} else {
				agent.send(prepareReply(message, mapToAllocatedJobsWithRejection(acceptedJobs, refusedJobs), REFUSE));
			}

			acceptedJobs.forEach(job -> sendJobConfirmationMessage(mapToClientJob(job), facts, index));
		};

	}

	private void sendJobConfirmationMessage(final ClientJob job, final RuleSetFacts facts, final int index) {
		final double price = computeExecutionPrice(job, facts);
		final Pair<Instant, Double> executionEstimation = agentProps.getEstimatedEarliestJobStartTimeAndDuration(job);
		final Instant startTime = executionEstimation.getKey();
		final double powerConsumption = agentProps.getPowerConsumption(startTime, job.getExpectedEndTime());

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Estimated Server job price: {}.", price);
		logger.info("Estimated earliest job start time: {}. The estimated execution time will be {}.",
				startTime, executionEstimation.getValue());

		final ServerData estimatedExecutionData =
				mapToServerData(job, executionEstimation, agentProps.resources(), powerConsumption, price, UNKNOWN);
		agent.send(prepareJobConfirmationMessageForRMA(estimatedExecutionData, job, index,
				agentProps.getOwnerRegionalManagerAgent()));
	}

	private Double computeExecutionPrice(final ClientJob job, final RuleSetFacts facts) {
		final RuleSetFacts priceFacts = constructFactsWithJob(facts.get(RULE_SET_IDX), job);
		priceFacts.put(RULE_TYPE, COMPUTE_PRICE_RULE);
		controller.fire(priceFacts);

		return priceFacts.get(RESULT);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMANewJobsAllocationCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
