package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.announcing;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.utils.facts.ProposalsFactsFactory.constructFactsForProposalsComparison;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CMA_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jeasy.rules.api.Facts;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentCFPRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class LookForRMAForJobExecutionRule extends AgentCFPRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(LookForRMAForJobExecutionRule.class);

	public LookForRMAForJobExecutionRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"initiate CFP in Regional Manager Agents",
				"when new job is to be announced in network, CMA sends CFP to all RMAs");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		return prepareCallForProposal(facts.get(JOB), agentProps.getAvailableRegionalManagers(),
				CMA_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final ClientJob job = facts.get(JOB);

		final RuleSetFacts comparatorFacts = constructFactsForProposalsComparison(
				agentProps.getRuleSetForJob().get(job.getJobId()), bestProposal, newProposal, JobWithPrice.class);
		controller.fire(comparatorFacts);

		return comparatorFacts.get(RESULT) instanceof Double doubleVal ?
				doubleVal.intValue() :
				comparatorFacts.get(RESULT);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {
		agent.send(prepareStringReply(proposalToReject, ((ClientJob) facts.get(JOB)).getJobId(), REJECT_PROPOSAL));
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

		agentProps.getClientJobs().replace(job, PROCESSING, ACCEPTED);
		agentProps.getRmaForJobMap().put(job.getJobId(), bestProposal.getSender());

		agent.send(prepareStringReply(bestProposal, job.getJobId(), ACCEPT_PROPOSAL));
	}

	private void handleFailure(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final RuleSetFacts failureFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		failureFacts.put(JOB, job);
		failureFacts.put(RULE_TYPE, LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE);
		controller.fire(failureFacts);
	}

	@Override
	public AgentRule copy() {
		return new LookForRMAForJobExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
