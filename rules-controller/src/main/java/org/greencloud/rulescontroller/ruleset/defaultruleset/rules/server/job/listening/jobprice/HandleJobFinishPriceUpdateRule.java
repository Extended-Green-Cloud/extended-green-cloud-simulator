package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobprice;

import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.MatchReplyWith;
import static jade.lang.acl.MessageTemplate.and;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.RuleType.FINAL_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FINAL_EXECUTION_PRICE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessageForRMA;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentSingleMessageListenerRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HandleJobFinishPriceUpdateRule extends AgentSingleMessageListenerRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(HandleJobFinishPriceUpdateRule.class);

	public HandleJobFinishPriceUpdateRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINAL_PRICE_RECEIVER_RULE,
				"listens for final job price",
				"listening for messages received from Green Source informing about final job execution price");

	}

	@Override
	protected MessageTemplate constructMessageTemplate(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return and(MatchReplyWith(message.getReplyWith()), MatchProtocol(FINAL_EXECUTION_PRICE_MESSAGE));
	}

	@Override
	protected long specifyExpirationTime(final RuleSetFacts facts) {
		return 5000;
	}

	@Override
	protected void handleMessageProcessing(final ACLMessage message, final RuleSetFacts facts) {
		final JobInstanceWithPrice jobWithPrice = readMessageContent(message, JobInstanceWithPrice.class);
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received final energy cost {} related to execution of job {}", jobWithPrice.getPrice(),
				job.getJobId());
		agentProps.updateJobEnergyCost(jobWithPrice);
		agentProps.updateJobExecutionCost(job);
		updateStateAfterJobIsDone(facts);

		final Double finalJobPrice = agentProps.getTotalPriceForJob().get(job.getJobId());
		final ACLMessage rmaMessage = prepareJobFinishMessageForRMA(job, facts.get(RULE_SET_IDX), finalJobPrice,
				agentProps.getOwnerRegionalManagerAgent());

		agentProps.getTotalPriceForJob().remove(job.getJobId());
		agent.send(rmaMessage);
	}

	private void updateStateAfterJobIsDone(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final JobInstanceIdentifier jobInstance = mapClientJobToJobInstanceId(job);

		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(jobInstance, FINISH);
		}

		agentProps.getGreenSourceForJobMap().remove(job.getJobId());
		agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().size() == 0) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}

		agentProps.updateGUI();
	}

}
