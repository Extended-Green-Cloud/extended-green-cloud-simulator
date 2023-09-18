package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.job.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class HandleJobFinishRule extends AgentScheduledRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(HandleJobFinishRule.class);

	public HandleJobFinishRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINISH_JOB_EXECUTION_RULE,
				"start of job execution in Server",
				"rule initiates start of Job execution in given Server");
	}

	@Override
	protected boolean evaluateBeforeTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		return agentProps.getServerJobs().containsKey(job)
				&& ACCEPTED_JOB_STATUSES.contains(agentProps.getServerJobs().get(job));
	}

	@Override
	protected Date specifyTime(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant endDate = getCurrentTime().isAfter(job.getEndTime()) ? getCurrentTime() : job.getEndTime();
		return Date.from(endDate);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final String jobId = job.getJobId();

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Finished executing the job {} at {}", job.getJobId(), job.getEndTime());

		facts.put(RULE_TYPE, PROCESS_FINISH_JOB_EXECUTION_RULE);
		controller.fire(facts);
	}
}
