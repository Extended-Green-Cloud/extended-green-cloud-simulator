package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.sourcepowershortage;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_CAUSE;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_IS_FINISHED;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.event.PowerShortageCauseEnum.PHYSICAL_CAUSE;
import static org.greencloud.commons.enums.event.PowerShortageCauseEnum.WEATHER_CAUSE;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_START_RULE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.findJobsWithinPower;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.enums.event.PowerShortageCauseEnum;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessGreenSourcePowerShortageStartEventRule extends
		AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessGreenSourcePowerShortageStartEventRule.class);

	public ProcessGreenSourcePowerShortageStartEventRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE, POWER_SHORTAGE_ERROR_START_RULE,
				"handle power shortage start event",
				"rule handles different cases of power shortage event");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final boolean isFinished = facts.get(EVENT_IS_FINISHED);
		return !isFinished;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final PowerShortageCauseEnum cause = facts.get(EVENT_CAUSE);
		final Instant startTime = facts.get(EVENT_TIME);
		final double maxPower = facts.get(RESULT);

		if (cause.equals(WEATHER_CAUSE)) {
			agentProps.getWeatherShortagesCounter().getAndIncrement();
		}
		agentProps.getShortagesAccumulator().getAndIncrement();

		final String logMessage = cause.equals(PHYSICAL_CAUSE) ?
				"Power shortage was detected! Power shortage will happen at: {}" :
				"Weather-caused power shortage was detected! Power shortage will happen at: {}";

		MDC.put(MDC_AGENT_NAME, agent.getLocalName());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info(logMessage, startTime);

		final List<ServerJob> affectedJobs = getAffectedServerJobs(startTime);

		if (affectedJobs.isEmpty()) {
			logger.info("Power shortage won't affect any jobs");
			initiatePowerShortageHandler(emptyList(), facts);
		} else {
			final List<ServerJob> jobsToKeep = findJobsWithinPower(affectedJobs, maxPower);
			final List<ServerJob> jobsToTransfer = prepareJobTransfer(affectedJobs, jobsToKeep);

			if (jobsToTransfer.isEmpty()) {
				logger.info("Power shortage won't affect any jobs");
				initiatePowerShortageHandler(emptyList(), facts);
				return;
			}

			affectedJobs.stream().parallel().forEach(serverJob -> {
				final int ruleSetForJob = agentProps.getRuleSetForJob().get(serverJob.getJobInstanceId());
				facts.put(RULE_SET_IDX, ruleSetForJob);

				MDC.put(MDC_JOB_ID, serverJob.getJobId());
				logger.info("Requesting job {} transfer in parent Server", serverJob.getJobId());

				final RuleSetFacts transferFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
				transferFacts.put(JOB, serverJob);
				transferFacts.put(EVENT_TIME, startTime);

				agent.addBehaviour(InitiateRequest.create(agent, transferFacts, TRANSFER_JOB_RULE, controller));
				agentProps.updateGUI();
			});
			initiatePowerShortageHandler(affectedJobs, facts);
		}
	}

	private List<ServerJob> prepareJobTransfer(final List<ServerJob> affectedJobs, final List<ServerJob> jobsToKeep) {
		return new ArrayList<>(affectedJobs.stream()
				.filter(job -> !jobsToKeep.contains(job))
				.toList());
	}

	private void initiatePowerShortageHandler(final List<ServerJob> jobsToTransfer, final RuleSetFacts facts) {
		final RuleSetFacts handleShortageFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handleShortageFacts.put(EVENT_TIME, facts.get(EVENT_TIME));
		handleShortageFacts.put(JOBS, jobsToTransfer);
		handleShortageFacts.put(SET_EVENT_ERROR, facts.get(EVENT_CAUSE).equals(PHYSICAL_CAUSE));

		agent.addBehaviour(ScheduleOnce.create(agent, handleShortageFacts, HANDLE_POWER_SHORTAGE_RULE, controller,
				SELECT_BY_FACTS_IDX));
	}

	private List<ServerJob> getAffectedServerJobs(final Instant startTime) {
		return agentProps.getServerJobs().keySet().stream()
				.filter(job -> startTime.isBefore(job.getEndTime()))
				.filter(job -> EXECUTING_ON_GREEN.getStatuses().contains(agentProps.getServerJobs().get(job)))
				.toList();
	}
}
