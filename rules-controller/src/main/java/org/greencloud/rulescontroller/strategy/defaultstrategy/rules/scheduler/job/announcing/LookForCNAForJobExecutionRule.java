package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SCHEDULER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Comparator;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.scheduler.SchedulerNode;

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
	protected ACLMessage createCFPMessage(final StrategyFacts facts) {
		return prepareCallForProposal(facts.get(JOB), agentProps.getAvailableCloudNetworks(),
				SCHEDULER_JOB_CFP_PROTOCOL, facts.get(STRATEGY_IDX));
	}

	@Override
	protected int compareProposals(final StrategyFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final Comparator<JobWithPrice> comparator = (msg1, msg2) ->
				(int) (msg1.getPriceForJob() - msg2.getPriceForJob());
		return compareMessages(bestProposal, newProposal, JobWithPrice.class, comparator);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final StrategyFacts facts) {
		agent.send(
				prepareReply(proposalToReject, JobMapper.mapClientJobToJobInstanceId(facts.get(JOB)), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final StrategyFacts facts) {
		handleFailure(facts);
	}

	@Override
	protected void handleNoProposals(final StrategyFacts facts) {
		handleFailure(facts);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Sending ACCEPT_PROPOSAL to {}", bestProposal.getSender().getName());

		agentProps.getClientJobs().replace(job, PROCESSING, ACCEPTED);
		agentProps.getCnaForJobMap().put(job.getJobId(), bestProposal.getSender());
		agent.send(prepareReply(bestProposal, JobMapper.mapClientJobToJobInstanceId(job), ACCEPT_PROPOSAL));
	}

	private void handleFailure(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final StrategyFacts failureFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		failureFacts.put(JOB, job);
		failureFacts.put(RULE_TYPE, LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE);
		controller.fire(failureFacts);
	}
}
