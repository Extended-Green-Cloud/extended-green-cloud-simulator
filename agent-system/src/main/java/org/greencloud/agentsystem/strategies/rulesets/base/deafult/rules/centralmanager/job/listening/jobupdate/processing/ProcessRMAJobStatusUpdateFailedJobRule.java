package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.listening.jobupdate.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessRMAJobStatusUpdateFailedJobRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessRMAJobStatusUpdateFailedJobRule.class);

	public ProcessRMAJobStatusUpdateFailedJobRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE,
				"handles job update - failed job",
				"rule runs when execution of new client job has failed");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final String type = facts.get(MESSAGE_TYPE);
		return type.equals(FAILED_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

			if (!agentProps.isPossiblyAfterDeadline(job)) {
				logger.info("Execution of the job {} has failed. Retrying to execute the job", job.getJobId());

				if (agentProps.getJobsToBeExecuted().offer(job)) {
					agentProps.getClientJobs().replace(job, CREATED);
				}
			} else {
				logger.info("Execution of the job {} has failed. Passing information to client.", job.getJobId());

				final int ruleSetIdx = agentProps.removeJob(job);
				controller.removeRuleSet(agentProps.getRuleSetForJob(), ruleSetIdx);
				agentProps.getRmaForJobMap().remove(job.getJobId());

				agent.send(
						prepareJobStatusMessageForClient(job, jobStatusUpdate, FAILED_JOB_ID, facts.get(RULE_SET_IDX)));
			}
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMAJobStatusUpdateFailedJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
