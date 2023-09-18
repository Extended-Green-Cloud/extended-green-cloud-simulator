package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.job.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.replaceStatusToActive;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessPowerSupplyStartRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessPowerSupplyStartRule.class);

	private ServerJob job;
	private JobInstanceIdentifier jobInstance;

	public ProcessPowerSupplyStartRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE,
				"handles power supply updates - started",
				"handling new updates regarding provided power supply coming from Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		job = facts.get(JOB);
		jobInstance = facts.get(JOB_ID);
		final String type = facts.get(MESSAGE_TYPE);
		return type.equals(STARTED_JOB_ID);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Started the execution of the job with id {}", jobInstance.getJobId());

		replaceStatusToActive(agentProps.getServerJobs(), job);
		agentProps.incrementJobCounter(jobInstance, STARTED);
	}
}
