package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.job.execution;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemovalWithPrice;
import static org.greencloud.commons.utils.job.JobUtils.calculateExpectedJobEndTime;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareManualFinishMessageForServer;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessManualPowerSupplyFinishRule extends AgentScheduledRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessManualPowerSupplyFinishRule.class);

	public ProcessManualPowerSupplyFinishRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_MANUAL_FINISH_RULE,
				"handle manual finish of job power supply",
				"rule executes handler which completes job power supply manually when no information from Server is received");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		return calculateExpectedJobEndTime(job);
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final boolean isJobPresent = agentProps.getServerJobs().containsKey(job);
		return isJobPresent && ACCEPTED_JOB_STATUSES.contains(agentProps.getServerJobs().get(job));
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.error("The power delivery of {} should be finished! Finishing power delivery by hand.",
				mapToJobInstanceId(job));

		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(job.getJobId(), FINISH);
		}
		controller.fire(constructFactsForJobRemovalWithPrice(facts.get(RULE_SET_IDX), job, true));
		agentProps.updateGUI();
		agent.send(
				prepareManualFinishMessageForServer(mapToJobInstanceId(job), job.getServer(), facts.get(RULE_SET_IDX)));
	}

	@Override
	public AgentRule copy() {
		return new ProcessManualPowerSupplyFinishRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
