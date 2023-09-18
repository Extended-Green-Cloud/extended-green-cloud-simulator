package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.events.shortagegreensource;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.POWER_SHORTAGE_SOURCE_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessPowerShortageFinishRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageFinishRule.class);

	private ClientJob job;
	private JobInstanceIdentifier jobInstance;

	public ProcessPowerShortageFinishRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_POWER_SHORTAGE_FINISH_HANDLER_RULE,
				"handlers finish of power shortage in Green Source",
				"rule handles information that power shortage has finished in the Green Source");
	}



	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		jobInstance = facts.get(MESSAGE_CONTENT);
		job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		return nonNull(job) && POWER_SHORTAGE_SOURCE_STATUSES.contains(agentProps.getServerJobs().get(job));
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Received the information that the power shortage is finished. Supplying job {} with green energy",
				job.getJobId());

		final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());
		agentProps.getServerJobs().replace(job, EXECUTING_ON_GREEN.getStatus(hasStarted));
		agentProps.updateGUI();
		agent.send(prepareJobStatusMessageForCNA(jobInstance, GREEN_POWER_JOB_ID, agentProps, facts.get(STRATEGY_IDX)));
	}
}
