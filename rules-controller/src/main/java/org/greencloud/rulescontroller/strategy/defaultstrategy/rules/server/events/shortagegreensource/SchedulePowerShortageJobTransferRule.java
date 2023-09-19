package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.shortagegreensource;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_FINISH_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_POWER_SHORTAGE_TRANSFER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.constants.LoggingConstants;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.core.AID;

public class SchedulePowerShortageJobTransferRule extends AgentScheduledRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(SchedulePowerShortageJobTransferRule.class);

	public SchedulePowerShortageJobTransferRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_POWER_SHORTAGE_TRANSFER_RULE,
				"handle power shortage transfer",
				"rule performs transfer of the job between Green Sources");
	}

	@Override
	protected Date specifyTime(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant transferTime = alignStartTimeToCurrentTime(job.getStartTime());
		return Date.from(transferTime);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobDivided<ClientJob> newJobInstances = facts.get(JOBS);
		final AID newGreenSource = facts.get(AGENT);

		MDC.put(LoggingConstants.MDC_JOB_ID, job.getJobId());
		finishPreviousInstance(newJobInstances, facts);

		if (agentProps.getServerJobs().containsKey(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("Transferring job between green sources");
			agentProps.getGreenSourceForJobMap().replace(job.getJobId(), newGreenSource);
			agent.send(prepareJobStatusMessageForCNA(JobMapper.mapClientJobToJobInstanceId(job), GREEN_POWER_JOB_ID, agentProps,
					facts.get(STRATEGY_IDX)));
			agentProps.updateGUI();

			if (isJobStarted(job, agentProps.getServerJobs())) {
				agent.send(prepareJobStartedMessage(job, facts.get(STRATEGY_IDX), newGreenSource));
			}
		}
	}

	private void finishPreviousInstance(final JobDivided<ClientJob> newJobInstances, final Facts facts) {
		final StrategyFacts finishFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		finishFacts.put(JOB, newJobInstances.getFirstInstance());
		finishFacts.put(JOB_FINISH_INFORM, false);
		finishFacts.put(RULE_TYPE, PROCESS_FINISH_JOB_EXECUTION_RULE);

		controller.fire(finishFacts);
		agentProps.updateGUI();
	}
}
