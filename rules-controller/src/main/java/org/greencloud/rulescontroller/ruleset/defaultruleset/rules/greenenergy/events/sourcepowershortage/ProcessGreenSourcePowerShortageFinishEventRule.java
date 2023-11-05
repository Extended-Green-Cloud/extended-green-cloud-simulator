package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.sourcepowershortage;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_CAUSE;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_IS_FINISHED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.event.PowerShortageCauseEnum.PHYSICAL_CAUSE;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_WEATHER_FOR_POWER_SHORTAGE_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.enums.event.PowerShortageCauseEnum;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessGreenSourcePowerShortageFinishEventRule
		extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessGreenSourcePowerShortageFinishEventRule.class);

	public ProcessGreenSourcePowerShortageFinishEventRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE, POWER_SHORTAGE_ERROR_FINISH_RULE,
				"handle power shortage finish event",
				"rule handles different cases of power shortage event");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final boolean isFinished = facts.get(EVENT_IS_FINISHED);
		final PowerShortageCauseEnum cause = facts.get(EVENT_CAUSE);
		return isFinished && cause.equals(PHYSICAL_CAUSE);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Power shortage has finished!!! Supplying jobs with green power");
		final List<ServerJob> jobsOnHold = agentProps.getActiveJobsOnHold(agentProps.getServerJobs());

		if (jobsOnHold.isEmpty()) {
			logger.info("There are no jobs which were on hold. Updating the maximum power");
		} else {
			jobsOnHold.forEach(powerJob -> {
				MDC.put(MDC_JOB_ID, powerJob.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				if (agentProps.getServerJobs().containsKey(powerJob)) {
					final int ruleSetForJob = agentProps.getRuleSetForJob().get(powerJob);
					facts.put(RULE_SET_IDX, ruleSetForJob);

					logger.info("Checking if the job {} can be put in progress", powerJob.getJobId());
					checkIfJobCanBePutOnGreenEnergy(powerJob, facts);

				} else {
					logger.info("Job {} has ended before supplying it back with green energy", powerJob.getJobId());
				}
			});
		}
		agentProps.setHasError(false);
	}

	private void checkIfJobCanBePutOnGreenEnergy(final ServerJob job, final RuleSetFacts facts) {
		final RuleSetFacts verificationFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		verificationFacts.put(JOB, job);
		agent.addBehaviour(
				InitiateRequest.create(agent, verificationFacts, CHECK_WEATHER_FOR_POWER_SHORTAGE_FINISH_RULE,
						controller));
	}
}
