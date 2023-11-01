package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
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

public class ProcessServerJobStatusUpdateFailedJobRule
		extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessServerJobStatusUpdateFailedJobRule.class);

	public ProcessServerJobStatusUpdateFailedJobRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, 7);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE,
				"handle failed job",
				"rule run when Server sends update regarding job failure");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return facts.get(MESSAGE_TYPE).equals(FAILED_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Sending information that the job {} execution has failed", job.getJobId());

			if (!agentProps.getNetworkJobs().get(job).equals(PROCESSING)) {
				agentNode.removePlannedJob();
			}
			final RuleSetFacts jobRemovalFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
			jobRemovalFacts.put(JOB, job);
			controller.fire(jobRemovalFacts);

			agentProps.getServerForJobMap().remove(job.getJobId());
			agentProps.incrementJobCounter(JobMapper.mapClientJobToJobInstanceId(job), FAILED);
			agentNode.updateGUI(agentProps);
			agent.send(prepareJobStatusMessageForScheduler(agentProps, jobStatusUpdate, FAILED_JOB_ID,
					facts.get(RULE_SET_IDX)));
		}
	}
}
