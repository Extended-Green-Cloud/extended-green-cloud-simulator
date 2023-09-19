package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.job.proposing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.domain.job.extended.ImmutableJobWithPrice;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

import jade.lang.acl.ACLMessage;

public class ProposeToSchedulerRule extends AgentProposalRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProposeToSchedulerRule.class);

	public ProposeToSchedulerRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to Scheduler",
				"rule sends proposal message to Scheduler and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final StrategyFacts facts) {
		final ACLMessage selectedOfferMessage = facts.get(MESSAGE);
		final ServerData selectedOffer = readMessageContent(selectedOfferMessage, ServerData.class);

		final JobWithPrice pricedJob =
				new ImmutableJobWithPrice(selectedOffer.getJobId(), selectedOffer.getServicePrice());
		return MessageBuilder.builder((int) facts.get(STRATEGY_IDX))
				.copy(((ACLMessage) facts.get(ORIGINAL_MESSAGE)).createReply())
				.withObjectContent(pricedJob)
				.withPerformative(PROPOSE)
				.build();
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final StrategyFacts facts) {
		final JobInstanceIdentifier jobInstance = readMessageContent(accept, JobInstanceIdentifier.class);
		final ClientJob job = getJobById(jobInstance.getJobId(), agentProps.getNetworkJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, jobInstance.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("Sending ACCEPT_PROPOSAL to Server Agent");

			agentProps.incrementJobCounter(jobInstance, ACCEPTED);
			agent.send(prepareAcceptJobOfferReply(facts.get(MESSAGE), jobInstance, SERVER_JOB_CFP_PROTOCOL));
		}
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final StrategyFacts facts) {
		final JobInstanceIdentifier jobInstance = readMessageContent(reject, JobInstanceIdentifier.class);
		final ClientJob job = getJobById(jobInstance.getJobId(), agentProps.getNetworkJobs());

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Scheduler {} rejected the job proposal", reject.getSender().getName());

		if (nonNull(job)) {
			agentProps.getServerForJobMap().remove(jobInstance.getJobId());
			agent.send(prepareReply(facts.get(MESSAGE), jobInstance, REJECT_PROPOSAL));

			final StrategyFacts jobRemovalFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
			jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
			jobRemovalFacts.put(JOB, job);
			controller.fire(jobRemovalFacts);
		}
	}
}
