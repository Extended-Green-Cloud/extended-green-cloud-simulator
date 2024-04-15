package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessRMAJobStatusUpdateFailedJobRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessRMAJobStatusUpdateFinishedJobRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessRMAJobStatusUpdateOtherStatusRule;
import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.listening.processing.ProcessRMAJobStatusUpdateStartedJobRule;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessRMAJobStatusUpdateCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessRMAJobStatusUpdateCombinedRule.class);

	public ProcessRMAJobStatusUpdateCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handles update regarding client job status",
				"rule run when Scheduler process message with updated client job status");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessRMAJobStatusUpdateStartedJobRule(controller),
				new ProcessRMAJobStatusUpdateFinishedJobRule(controller),
				new ProcessRMAJobStatusUpdateFailedJobRule(controller),
				new ProcessRMAJobStatusUpdateOtherStatusRule(controller)
		);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);
		final String jobId = jobStatusUpdate.getJobInstance().getJobId();

		MDC.put(MDC_JOB_ID, jobStatusUpdate.getJobInstance().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received update regarding job {} state. Passing the information to client.", jobId);

		final ClientJob job = getJobById(jobId, agentProps.getClientJobs());
		facts.put(JOB, ofNullable(job));
	}
}
