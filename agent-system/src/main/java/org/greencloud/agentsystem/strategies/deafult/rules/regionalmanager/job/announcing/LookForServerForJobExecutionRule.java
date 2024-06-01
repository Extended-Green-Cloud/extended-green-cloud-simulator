package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.announcing;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.utils.facts.ProposalsFactsFactory.constructFactsForProposalsComparison;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.RMA_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentCFPRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class LookForServerForJobExecutionRule extends AgentCFPRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(LookForServerForJobExecutionRule.class);

	public LookForServerForJobExecutionRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"look for server that will execute Client Job",
				"rule run when Regional Manager Agent receives new Client Job");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		final List<AID> consideredServers = agentProps.getOwnedActiveServers();
		return prepareCallForProposal(facts.get(JOB), consideredServers, RMA_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final ClientJob job = facts.get(JOB);

		final RuleSetFacts comparatorFacts = constructFactsForProposalsComparison(
				agentProps.getRuleSetForJob().get(job.getJobInstanceId()), bestProposal, newProposal, ServerData.class);
		comparatorFacts.put(JOB, job);
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
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No responses from servers were retrieved.");
		agentProps.getJobsToBeExecuted().add(job);
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("All servers refused to execute the job. Putting the job back to the queue.");
		agentProps.getJobsToBeExecuted().add(job);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Chosen Server for the job {}: {}.", job.getJobId(), bestProposal.getSender().getName());
		agentProps.getServerForJobMap().put(job.getJobId(), bestProposal.getSender());

		agent.send(prepareStringReply(bestProposal, job.getJobId(), ACCEPT_PROPOSAL));
	}

	@Override
	public AgentRule copy() {
		return new LookForServerForJobExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
