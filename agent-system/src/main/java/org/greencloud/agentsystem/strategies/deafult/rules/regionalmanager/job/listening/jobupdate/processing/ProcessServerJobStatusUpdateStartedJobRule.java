package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.listening.jobupdate.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE;
import static org.greencloud.commons.mapper.JobStatusMapper.mapToJobWithStatusForServer;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.job.JobUtils.updateJobStartAndExecutionTime;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCMA;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessServerJobStatusUpdateStartedJobRule
		extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerJobStatusUpdateStartedJobRule.class);

	public ProcessServerJobStatusUpdateStartedJobRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 5);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE,
				"handle job started",
				"rule run when Server sends update regarding job start");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return facts.get(MESSAGE_TYPE).equals(STARTED_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final ClientJob receivedJob = readMessageContent(message, ClientJob.class);
		final ClientJob job = getJobById(receivedJob.getJobId(), agentProps.getNetworkJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Received information that execution of the job {} has started.", job.getJobId());

			if (!agentProps.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
				updateStatusForNewJob(job, receivedJob, message.getSender().getLocalName(), facts);
			}
		}
	}

	private void updateStatusForNewJob(final ClientJob job, final ClientJob receivedJob, final String server,
			final RuleSetFacts facts) {
		final JobWithStatus jobWithServer = mapToJobWithStatusForServer(receivedJob, server);
		final Instant jobStart = receivedJob.getStartTime();
		final long jobDuration = receivedJob.getDuration();

		agentProps.getNetworkJobs().replace(job, IN_PROGRESS);
		updateJobStartAndExecutionTime(job, jobStart, jobDuration, agentProps.getNetworkJobs());
		agentProps.incrementJobCounter(job.getJobId(), STARTED);
		agentNode.addStartedJob();

		agent.send(prepareJobStatusMessageForCMA(agentProps, jobWithServer, STARTED_JOB_ID, facts.get(RULE_SET_IDX)));
		agentNode.updateGUI(agentProps);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerJobStatusUpdateStartedJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
