package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.errorserver;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;
import com.gui.event.PowerShortageEvent;

public class ProcessPowerShortageFinishEventRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageFinishEventRule.class);

	public ProcessPowerShortageFinishEventRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE, POWER_SHORTAGE_ERROR_FINISH_RULE,
				"handle power shortage finish event",
				"rule handles finish of power shortage event");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final PowerShortageEvent powerShortageEvent = facts.get(EVENT);
		return powerShortageEvent.isFinished();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("Internal server error has finished! Supplying jobs with green power");
		agentProps.setHasError(false);
		final List<ClientJob> affectedJobs = agentProps.getActiveJobsOnHold(agentProps.getServerJobs());

		if (affectedJobs.isEmpty()) {
			logger.info("There are no jobs supplied using back up power. Updating server state.");
		} else {
			logger.info("Changing the statuses of the jobs and informing the CNA and Green Sources");

			affectedJobs.forEach(job -> {
				final boolean isJobPresent = agentProps.getServerJobs().containsKey(job)
						&& agentProps.getGreenSourceForJobMap().containsKey(job.getJobId());

				if (isJobPresent) {
					handlePowerShortageFinish(job, facts);
				}
			});
		}
	}

	private void handlePowerShortageFinish(final ClientJob job, final Facts facts) {
		final JobInstanceIdentifier jobInstance = JobMapper.mapClientJobToJobInstanceId(job);
		final HardwareResources availableResources = agentProps.getAvailableResources(job, jobInstance, null);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		if (!availableResources.areSufficient(job.getEstimatedResources())) {
			logger.info("There are not enough resources to continue the job processing! Leaving job {} on hold",
					job.getJobId());
		} else {
			logger.info("Processing job {} with green source energy!", job.getJobId());
			supplyJobWithGreenEnergy(job, jobInstance, facts);
		}
	}

	private void supplyJobWithGreenEnergy(final ClientJob job, final JobInstanceIdentifier jobInstance,
			final Facts facts) {
		final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());

		agentProps.getServerJobs().replace(job, EXECUTING_ON_GREEN.getStatus(hasStarted));

		if (hasStarted) {
			agent.send(prepareJobStatusMessageForCNA(jobInstance, GREEN_POWER_JOB_ID, agentProps,
					facts.get(STRATEGY_IDX)));
		}
		agentProps.updateGUI();
		agent.send(prepareNetworkFailureInformation(jobInstance, NETWORK_ERROR_FINISH_ALERT_PROTOCOL,
				facts.get(STRATEGY_IDX),
				agentProps.getGreenSourceForJobMap().get(job.getJobId()),
				agentProps.getOwnerCloudNetworkAgent()));
	}
}
