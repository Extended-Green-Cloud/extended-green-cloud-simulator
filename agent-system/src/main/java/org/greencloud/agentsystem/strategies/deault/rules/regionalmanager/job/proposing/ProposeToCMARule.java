package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.proposing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemoval;
import static org.greencloud.commons.utils.facts.PriorityFactsFactory.constructFactsForPriorityPreEvaluation;
import static org.greencloud.commons.utils.facts.ProposalsFactsFactory.constructFactsForProposalMessage;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentProposalRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProposeToCMARule extends AgentProposalRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProposeToCMARule.class);

	public ProposeToCMARule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to CMA",
				"rule sends proposal message to CMA and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final RuleSetFacts proposalFacts =
				constructFactsForProposalMessage(facts.get(RULE_SET_IDX), facts.get(MESSAGE), job);
		controller.fire(proposalFacts);

		return proposalFacts.get(RESULT);
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final RuleSetFacts facts) {
		final String jobId = accept.getContent();
		final ClientJob job = getJobById(jobId, agentProps.getNetworkJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, jobId);
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("RMA was selected for job execution. Job is being pre-processed before adding to the queue.");
			controller.fire(constructFactsForPriorityPreEvaluation(facts.get(RULE_SET_IDX), job));
		}
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final RuleSetFacts facts) {
		final String jobId = reject.getContent();
		final ClientJob job = getJobById(jobId, agentProps.getNetworkJobs());

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("CMA {} rejected the job proposal.", reject.getSender().getName());

		ofNullable(job).ifPresent(clientJob ->
				controller.fire(constructFactsForJobRemoval(facts.get(RULE_SET_IDX), clientJob)));
	}

	@Override
	public AgentRule copy() {
		return new ProposeToCMARule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
