package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.RuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ACCEPTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SCHEDULER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class LookForCNAForJobExecutionRule extends AgentCFPRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(LookForCNAForJobExecutionRule.class);

	public LookForCNAForJobExecutionRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"initiate CFP in Cloud Network Agents",
				"when new job is to be announced in network, Scheduler sends CFP to all CNAs");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		return prepareCallForProposal(facts.get(JOB), agentProps.getAvailableCloudNetworks(),
				SCHEDULER_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final ClientJob job = facts.get(JOB);
		final JobWithPrice bestOfferContent = readMessageContent(bestProposal, JobWithPrice.class);
		final JobWithPrice newOfferContent = readMessageContent(newProposal, JobWithPrice.class);

		final RuleSetFacts comparatorFacts = new RuleSetFacts(agentProps.getRuleSetForJob().get(job.getJobId()));
		comparatorFacts.put(RULE_TYPE, COMPARE_EXECUTION_PROPOSALS);
		comparatorFacts.put("BEST_PROPOSAL_CONTENT", bestOfferContent);
		comparatorFacts.put("NEW_PROPOSAL_CONTENT", newOfferContent);
		controller.fire(comparatorFacts);

		return comparatorFacts.get(RESULT) instanceof Double doubleVal ?
				doubleVal.intValue() :
				comparatorFacts.get(RESULT);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {
		agent.send(
				prepareReply(proposalToReject, JobMapper.mapClientJobToJobInstanceId(facts.get(JOB)), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final RuleSetFacts facts) {
		handleFailure(facts);
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		handleFailure(facts);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Sending ACCEPT_PROPOSAL to {}", bestProposal.getSender().getName());

		final JobWithPrice bestOfferContent = readMessageContent(bestProposal, JobWithPrice.class);
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobInstance(mapToJobInstanceId(job))
				.priceForJob(bestOfferContent.getPriceForJob())
				.changeTime(getCurrentTime())
				.build();

		agentProps.getClientJobs().replace(job, PROCESSING, ACCEPTED);
		agentProps.getCnaForJobMap().put(job.getJobId(), bestProposal.getSender());
		agent.send(prepareJobStatusMessageForClient(job, jobStatusUpdate, ACCEPTED_JOB_ID,
				facts.get(RULE_SET_IDX)));
		agent.send(prepareReply(bestProposal, JobMapper.mapClientJobToJobInstanceId(job), ACCEPT_PROPOSAL));
	}

	private void handleFailure(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final RuleSetFacts failureFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		failureFacts.put(JOB, job);
		failureFacts.put(RULE_TYPE, LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE);
		controller.fire(failureFacts);
	}
}
