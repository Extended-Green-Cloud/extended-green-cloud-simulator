package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.transfer.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REFUSED_TRANSFER_JOB_NOT_FOUND_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

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
	public boolean evaluateRule(final RuleSetFacts facts) {
		final String content = facts.get(MESSAGE_CONTENT);
		final ServerJob job = facts.get(JOB);

		if (content.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("The job {} does not exist anymore. Finishing the job.", job.getJobInstanceId());

			return agentProps.getServerJobs().containsKey(job);
		}
		return false;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		agentProps.getServerJobs().entrySet().removeIf(entry -> {
			final ServerJob serverJob = entry.getKey();
			final String jobId = serverJob.getJobId();

			if (isPreviousJobInstant(job, serverJob)) {
				if (isJobStarted(entry.getValue())) {
					agentProps.incrementJobCounter(jobId, FINISH);
				}
				return true;
			}
			return false;
		});
		agentProps.updateGUI();
	}

	private boolean isPreviousJobInstant(final ServerJob currentJobInstance, final ServerJob jobInstance) {
		final String jobId = jobInstance.getJobId();

		return jobId.equals(currentJobInstance.getJobId()) &&
				nonNull(currentJobInstance.getStartTime()) &&
				nonNull(jobInstance.getStartTime()) &&
				!jobInstance.getStartTime().isAfter(currentJobInstance.getStartTime());
	}

	@Override
	public AgentRule copy() {
		return new ProcessTransferRefuseJobNotFoundRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
