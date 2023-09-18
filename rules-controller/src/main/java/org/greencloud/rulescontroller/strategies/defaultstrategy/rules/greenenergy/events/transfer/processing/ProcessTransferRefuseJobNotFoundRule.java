package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.events.transfer.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_NOT_FOUND_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
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

public class ProcessTransferRefuseJobNotFoundRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessTransferRefuseJobNotFoundRule.class);

	public ProcessTransferRefuseJobNotFoundRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REFUSED_TRANSFER_JOB_RULE, REFUSED_TRANSFER_JOB_NOT_FOUND_RULE,
				"process refused job transfer request - job not found",
				"rule processes refusal of job transfer request in Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final String content = facts.get(MESSAGE_CONTENT);
		final ServerJob job = facts.get(JOB);

		if (content.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("The job {} does not exist anymore. Finishing the job.", job);

			return agentProps.getServerJobs().containsKey(job);
		}
		return false;
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		agentProps.getServerJobs().entrySet().removeIf(entry -> {
			final ServerJob serverJob = entry.getKey();
			final String jobId = serverJob.getJobId();

			if (jobId.equals(job.getJobId()) && !serverJob.getStartTime().isAfter(job.getStartTime())) {
				if (isJobStarted(entry.getValue())) {
					agentProps.incrementJobCounter(mapToJobInstanceId(job), FINISH);
				}
				return true;
			}
			return false;
		});
		agentProps.updateGUI();
	}
}
