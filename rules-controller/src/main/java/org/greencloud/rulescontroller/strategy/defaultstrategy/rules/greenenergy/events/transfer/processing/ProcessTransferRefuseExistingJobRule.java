package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.events.transfer.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_EXISTING_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessTransferRefuseExistingJobRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessTransferRefuseExistingJobRule.class);

	public ProcessTransferRefuseExistingJobRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REFUSED_TRANSFER_JOB_RULE, REFUSED_TRANSFER_JOB_EXISTING_JOB_RULE,
				"process refused job transfer request",
				"rule processes refusal of job transfer request in Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		final String content = facts.get(MESSAGE_CONTENT);
		return agentProps.getServerJobs().containsKey(job) && !content.equals(JOB_NOT_FOUND_CAUSE_MESSAGE);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		final boolean hasJobStarted = isJobStarted(job, agentProps.getServerJobs());
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Transfer of job with id {} was unsuccessful! Putting the job on hold.", job.getJobId());

		agentProps.getServerJobs().replace(job, EXECUTING_ON_HOLD.getStatus(hasJobStarted));
		agentProps.updateGUI();
	}
}
