package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessServerJobStatusUpdateStartedJobRule
		extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessServerJobStatusUpdateStartedJobRule.class);

	public ProcessServerJobStatusUpdateStartedJobRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
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
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Sending information that the job {} execution has started", job.getJobId());

			if (!agentProps.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
				agentProps.getNetworkJobs().replace(job, IN_PROGRESS);
				agentProps.incrementJobCounter(JobMapper.mapClientJobToJobInstanceId(job), STARTED);
				agentNode.addStartedJob();
			}
			agent.send(prepareJobStatusMessageForScheduler(agentProps, jobStatusUpdate, STARTED_JOB_ID,
					facts.get(RULE_SET_IDX)));
		}
	}
}
