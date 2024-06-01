package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.listening.jobupdate.processing;

import static java.lang.String.valueOf;
import static java.util.Optional.of;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_IS_PRESENT;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemoval;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCMA;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessServerJobStatusUpdateFinishedJobRule
		extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerJobStatusUpdateFinishedJobRule.class);

	public ProcessServerJobStatusUpdateFinishedJobRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 4);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE,
				"handle finished job",
				"rule run when Server sends update regarding job finish");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return facts.get(MESSAGE_TYPE).equals(FINISH_JOB_ID) && ((boolean) facts.get(JOB_IS_PRESENT));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);
		final Double cost = agentProps.updatePriceForJob(job.getJobId(), jobStatusUpdate.getPriceForJob());

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Total cost of execution of the job {} was", cost);
		logger.info("Sending information that the job {} execution has finished", job.getJobId());

		if (isJobStarted(job, agentProps.getNetworkJobs())) {
			updateHighestTimeDifference(jobStatusUpdate, job);
			agentProps.incrementJobCounter(job.getJobId(), FINISH);
		}
		if (!PROCESSING.equals(agentProps.getNetworkJobs().get(job))) {
			agentNode.removeActiveJob();
			agentNode.removePlannedJob();
		}
		final JobWithStatus jobWithFinalCost = ImmutableJobWithStatus.copyOf(jobStatusUpdate).withPriceForJob(cost);
		controller.fire(constructFactsForJobRemoval(facts.get(RULE_SET_IDX), job));

		agentProps.getServerForJobMap().remove(job.getJobId());
		agentProps.updateGUI();
		agent.send(prepareJobStatusMessageForCMA(agentProps, jobWithFinalCost, FINISH_JOB_ID, facts.get(RULE_SET_IDX)));
	}

	private void updateHighestTimeDifference(final JobWithStatus jobStatusUpdate, final ClientJob job) {
		final long jobExecutionFinishTime = convertToRealTime(jobStatusUpdate.getChangeTime()).toEpochMilli();
		final long jobExecutionStartTime = convertToRealTime(job.getStartTime()).toEpochMilli();
		final long expectedDuration = job.getDuration();
		final long finalDuration = jobExecutionFinishTime - jobExecutionStartTime;
		final long msTimeDifference = finalDuration - expectedDuration;
		final AID server = agentProps.getServerForJobMap().get(job.getJobId());

		agentProps.getHighestExecutionTimeErrorForServer().computeIfPresent(server,
				(key, currDiff) -> of(currDiff).filter(diff -> diff >= msTimeDifference).orElse(msTimeDifference));
		agentProps.getHighestExecutionTimeErrorForServer().putIfAbsent(server, msTimeDifference);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerJobStatusUpdateFinishedJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
