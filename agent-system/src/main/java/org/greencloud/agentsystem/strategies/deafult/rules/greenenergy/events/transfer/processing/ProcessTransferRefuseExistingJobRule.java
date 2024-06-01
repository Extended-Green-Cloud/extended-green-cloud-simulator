package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.events.transfer.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REFUSED_TRANSFER_JOB_EXISTING_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

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
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final String content = facts.get(MESSAGE_CONTENT);
		return agentProps.getServerJobs().containsKey(job) && !content.equals(JOB_NOT_FOUND_CAUSE_MESSAGE);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final boolean hasJobStarted = isJobStarted(job, agentProps.getServerJobs());
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Transfer of job with id {} was unsuccessful! Putting the job on hold.", job.getJobId());
		final JobExecutionStatusEnum prevStatus = agentProps.getServerJobs().get(job);
		final JobExecutionStatusEnum newStatus = EXECUTING_ON_HOLD.getStatus(hasJobStarted);

		agentProps.getJobsExecutionTime().updateJobExecutionDuration(job, prevStatus, newStatus, getCurrentTime());
		agentProps.getServerJobs().replace(job, newStatus);
		agentProps.updateGUI();
	}

	@Override
	public AgentRule copy() {
		return new ProcessTransferRefuseExistingJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
