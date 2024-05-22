package org.greencloud.agentsystem.strategies.deault.rules.server.job.proposing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.OFFER;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.facts.ProposalsFactsFactory.constructFactsForProposalMessage;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobConfirmationMessageForRMA;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentProposalRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProposeToRMARule extends AgentProposalRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProposeToRMARule.class);

	public ProposeToRMARule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to RMA",
				"rule sends proposal message to RMA and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final RuleSetFacts proposalFacts =
				constructFactsForProposalMessage(facts.get(RULE_SET_IDX), facts.get(MESSAGE), job);
		controller.fire(proposalFacts);
		facts.put(OFFER, proposalFacts.get(OFFER));

		return proposalFacts.get(RESULT);
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final RuleSetFacts facts) {
		final String jobId = accept.getContent();
		final ClientJob job = getJobById(jobId, agentProps.getServerJobs());

		if (nonNull(job)) {
			agentProps.stoppedJobProcessing();
			agentProps.incrementJobCounter(job.getJobId(), ACCEPTED);
			agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Putting the job {} into job execution queue.", jobId);
			agentProps.getJobsForExecutionQueue().add(job);

			logger.info("Announcing new job {} in network!", job.getJobId());
			agentNode.announceClientJob();

			agent.send(prepareJobConfirmationMessageForRMA(facts.get(OFFER), job, facts.get(RULE_SET_IDX),
					agentProps.getOwnerRegionalManagerAgent()));
		}
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final RuleSetFacts facts) {
		final String jobId = reject.getContent();
		final ClientJob job = getJobById(jobId, agentProps.getServerJobs());

		if (nonNull(job)) {
			agentProps.stoppedJobProcessing();

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Regional Manager {} rejected the job volunteering offer", reject.getSender().getLocalName());
			agentProps.removeJob(job);

			if (agentProps.isDisabled() && agentProps.getServerJobs().isEmpty()) {
				logger.info("Server completed all planned jobs and is fully disabled.");
				agentNode.disableServer();
			}
		}
	}

	@Override
	public AgentRule copy() {
		return new ProposeToRMARule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
