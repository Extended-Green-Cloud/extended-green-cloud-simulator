package org.greencloud.agentsystem.strategies.deault.rules.server.job.listening.jobprice;

import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.MatchReplyWith;
import static jade.lang.acl.MessageTemplate.and;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINAL_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FINAL_EXECUTION_PRICE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessageForRMA;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.gui.agents.server.ServerNode;
import org.jeasy.rules.api.Facts;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSingleMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
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
		final String jobId = job.getJobId();

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received final energy cost {} related to execution of job {}", jobWithPrice.getPrice(), jobId);
		agentProps.updateJobEnergyCost(jobWithPrice);
		agentProps.updateJobExecutionCost(job);
		updateStateAfterJobIsDone(facts);

		final Double finalJobPrice = agentProps.getTotalPriceForJob().get(jobId);
		final ACLMessage rmaMessage = prepareJobFinishMessageForRMA(job, facts.get(RULE_SET_IDX), finalJobPrice,
				agentProps.getOwnerRegionalManagerAgent());

		agentProps.getTotalPriceForJob().remove(jobId);
		agent.send(rmaMessage);
	}

	private void updateStateAfterJobIsDone(final Facts facts) {
		final ClientJob job = facts.get(JOB);

		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(job.getJobId(), FINISH);
		}

		agentProps.getGreenSourceForJobMap().remove(job.getJobId());
		agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().isEmpty()) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}

		agentProps.updateGUI();
	}

	@Override
	public AgentRule copy() {
		return new HandleJobFinishPriceUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}

}
