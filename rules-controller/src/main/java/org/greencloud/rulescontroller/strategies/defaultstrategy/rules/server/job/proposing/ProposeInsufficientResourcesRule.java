package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.job.proposing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.INSUFFICIENT_RESOURCES_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProposeInsufficientResourcesRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProposeInsufficientResourcesRule.class);

	public ProposeInsufficientResourcesRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(INSUFFICIENT_RESOURCES_RULE,
				"handle insufficient resources in job proposal",
				"rule executed when Scheduler accepts job for execution, but Server has insufficient resources");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final String protocol = facts.get(MESSAGE_TYPE);
		final ClientJob job = facts.get(JOB);
		final ACLMessage cnaMessage = facts.get(ORIGINAL_MESSAGE);
		final ACLMessage greenSourceMessage = facts.get(MESSAGE);

		final JobInstanceIdentifier jobInstance = mapToJobInstanceId(job);
		final String responseProtocol = protocol.equals(POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL) ?
				FAILED_TRANSFER_PROTOCOL : FAILED_JOB_PROTOCOL;

		final JobWithStatus jobStatusUpdate = new ImmutableJobWithStatus(jobInstance, getCurrentTime());

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Not enough resources to execute the job {}. "
				+ "Sending failure information and rejecting green source proposal", job.getJobId());

		agentProps.removeJob(job);
		agentProps.getGreenSourceForJobMap().remove(job.getJobId());
		agent.send(prepareReply(greenSourceMessage, jobInstance, REJECT_PROPOSAL));
		agentProps.incrementJobCounter(jobInstance, FAILED);
		agent.send(prepareFailureReply(cnaMessage, jobStatusUpdate, responseProtocol));
	}
}
