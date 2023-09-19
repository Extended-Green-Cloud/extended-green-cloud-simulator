package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.replaceStatusToActive;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_HANDLE_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateCallForProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

import jade.lang.acl.ACLMessage;

public class ProcessNewClientJobOriginalTimeRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {
	private static final Logger logger = getLogger(ProcessNewClientJobOriginalTimeRule.class);

	public ProcessNewClientJobOriginalTimeRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE, NEW_JOB_ANNOUNCEMENT_HANDLE_JOB_RULE,
				"announce job with original time frames",
				"when job has correct time frames, CFP is sent to CNAs");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant newAdjustedStart = facts.get("job-adjusted-start");

		return !job.getStartTime().isBefore(newAdjustedStart);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Looking for Cloud Network for job {} execution", JobMapper.mapClientJobToJobInstanceId(job));

		final ACLMessage message = prepareJobStatusMessageForClient(job, PROCESSING_JOB_ID, facts.get(STRATEGY_IDX));
		replaceStatusToActive(agentProps.getClientJobs(), job);

		agent.send(message);
		agent.addBehaviour(InitiateCallForProposal.create(agent, facts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}
}
