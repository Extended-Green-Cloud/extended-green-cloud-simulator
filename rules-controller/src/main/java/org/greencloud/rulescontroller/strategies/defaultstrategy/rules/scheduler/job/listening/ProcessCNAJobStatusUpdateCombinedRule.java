package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening.processing.ProcessCNAJobStatusUpdateFailedJobRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening.processing.ProcessCNAJobStatusUpdateFinishedJobRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening.processing.ProcessCNAJobStatusUpdateOtherStatusRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening.processing.ProcessCNAJobStatusUpdateStartedJobRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessCNAJobStatusUpdateCombinedRule extends AgentCombinedRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessCNAJobStatusUpdateCombinedRule.class);

	public ProcessCNAJobStatusUpdateCombinedRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
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
				new ProcessCNAJobStatusUpdateStartedJobRule(controller),
				new ProcessCNAJobStatusUpdateFinishedJobRule(controller),
				new ProcessCNAJobStatusUpdateFailedJobRule(controller),
				new ProcessCNAJobStatusUpdateOtherStatusRule(controller)
		);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);
		final String jobId = jobStatusUpdate.getJobInstance().getJobId();

		MDC.put(MDC_JOB_ID, jobStatusUpdate.getJobInstance().getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Received update regarding job {} state. Passing the information to client.", jobId);

		final ClientJob job = getJobById(jobId, agentProps.getClientJobs());
		facts.put(JOB, ofNullable(job));
	}
}
