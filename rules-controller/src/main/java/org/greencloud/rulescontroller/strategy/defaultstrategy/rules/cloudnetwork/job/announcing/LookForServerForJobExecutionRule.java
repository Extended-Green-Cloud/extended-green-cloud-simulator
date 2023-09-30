package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.job.announcing;

import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.temporal.ValueRange;
import java.util.Collection;
import java.util.Comparator;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.cloudnetwork.CloudNetworkNode;

import jade.lang.acl.ACLMessage;

public class LookForServerForJobExecutionRule extends AgentCFPRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(LookForServerForJobExecutionRule.class);
	private static final ValueRange MAX_POWER_DIFFERENCE = ValueRange.of(-10, 10);

	public LookForServerForJobExecutionRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
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
				"rule run when Cloud Network Agent receives new Client Job");
	}

	@Override
	protected ACLMessage createCFPMessage(final StrategyFacts facts) {
		return prepareCallForProposal(facts.get(JOB), agentProps.getOwnedActiveServers(), CNA_JOB_CFP_PROTOCOL,
				facts.get(STRATEGY_IDX));
	}

	@Override
	protected int compareProposals(final StrategyFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final int weight1 = agentProps.getWeightsForServersMap().get(bestProposal.getSender());
		final int weight2 = agentProps.getWeightsForServersMap().get(newProposal.getSender());

		final Comparator<ServerData> comparator = (msg1, msg2) -> {
			final double powerDiff = (msg1.getPowerConsumption() * weight2) - (msg2.getPowerConsumption() * weight1);
			final double priceDiff = ((msg1.getServicePrice() * 1 / weight1) - (msg2.getServicePrice() * 1 / weight2));

			return MAX_POWER_DIFFERENCE.isValidIntValue((int) powerDiff) ? (int) priceDiff : (int) powerDiff;
		};

		return compareMessages(bestProposal, newProposal, ServerData.class, comparator);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final StrategyFacts facts) {
		agent.send(
				prepareReply(proposalToReject, JobMapper.mapClientJobToJobInstanceId(facts.get(JOB)), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("No responses from servers were retrieved");
		handleRejectedJob(facts);
	}

	@Override
	protected void handleNoProposals(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("All servers refused to execute the job - sending REFUSE response");
		handleRejectedJob(facts);

	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Chosen Server for the job {}: {}. Sending job execution offer to Scheduler Agent",
				job.getJobId(), bestProposal.getSender().getName());
		agentProps.getServerForJobMap().put(job.getJobId(), bestProposal.getSender());

		facts.put(ORIGINAL_MESSAGE, facts.get(MESSAGE));
		facts.put(MESSAGE, bestProposal);
		agent.addBehaviour(
				InitiateProposal.create(agent, FactsMapper.mapToStrategyFacts(facts), PROPOSE_TO_EXECUTE_JOB_RULE,
						controller));
	}

	private void handleRejectedJob(final StrategyFacts facts) {
		final StrategyFacts jobRemovalFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(JOB, facts.get(JOB));
		controller.fire(jobRemovalFacts);

		agentProps.updateGUI();
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
