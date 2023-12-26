package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.announcing;

import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.RMA_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class LookForServerForJobExecutionRule extends AgentCFPRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(LookForServerForJobExecutionRule.class);

	public LookForServerForJobExecutionRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"look for server that will execute Client Job",
				"rule run when Regional Manager Agent receives new Client Job");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		final List<AID> consideredServers = facts.get(AGENTS);
		return prepareCallForProposal(facts.get(JOB), consideredServers, RMA_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final ClientJob job = facts.get(JOB);
		final ServerData bestOfferContent = readMessageContent(bestProposal, ServerData.class);
		final ServerData newOfferContent = readMessageContent(newProposal, ServerData.class);

		final RuleSetFacts comparatorFacts = new RuleSetFacts(
				agentProps.getRuleSetForJob().get(job.getJobInstanceId()));
		comparatorFacts.put(RULE_TYPE, COMPARE_EXECUTION_PROPOSALS);
		comparatorFacts.put(JOB, job);
		comparatorFacts.put("BEST_PROPOSAL", bestProposal);
		comparatorFacts.put("NEW_PROPOSAL", newProposal);
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
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No responses from servers were retrieved");
		handleRejectedJob(facts);
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("All servers refused to execute the job - sending REFUSE response");
		handleRejectedJob(facts);

	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Chosen Server for the job {}: {}. Sending job execution offer to Scheduler Agent",
				job.getJobId(), bestProposal.getSender().getName());
		agentProps.getServerForJobMap().put(job.getJobId(), bestProposal.getSender());

		facts.put(ORIGINAL_MESSAGE, facts.get(MESSAGE));
		facts.put(MESSAGE, bestProposal);
		agent.addBehaviour(
				InitiateProposal.create(agent, FactsMapper.mapToRuleSetFacts(facts), PROPOSE_TO_EXECUTE_JOB_RULE,
						controller));
	}

	private void handleRejectedJob(final RuleSetFacts facts) {
		final RuleSetFacts jobRemovalFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(JOB, facts.get(JOB));
		controller.fire(jobRemovalFacts);

		agentProps.updateGUI();
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
