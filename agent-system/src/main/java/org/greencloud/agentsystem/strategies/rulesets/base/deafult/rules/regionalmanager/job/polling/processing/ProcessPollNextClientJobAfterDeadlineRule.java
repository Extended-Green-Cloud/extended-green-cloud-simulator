package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.polling.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFY_DEADLINE_RULE;
import static org.greencloud.commons.mapper.JobStatusMapper.mapToJobWithStatusForCurrentTime;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemoval;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCMA;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessPollNextClientJobAfterDeadlineRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessPollNextClientJobAfterDeadlineRule.class);

	public ProcessPollNextClientJobAfterDeadlineRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFY_DEADLINE_RULE,
				"sending failure when job is after deadline",
				"when job that is to be announced is after deadline, RMA sends FAILURE message");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return !job.getDeadline().isAfter(getCurrentTime());
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job would be executed after deadline. Sending failure to CMA.");

		final JobWithStatus jobStatusUpdate = mapToJobWithStatusForCurrentTime(job);
		agent.send(prepareJobStatusMessageForCMA(agentProps, jobStatusUpdate, FAILED_JOB_ID, facts.get(RULE_SET_IDX)));
		controller.fire(constructFactsForJobRemoval(facts.get(RULE_SET_IDX), facts.get(JOB)));
		agentProps.updateGUI();

		facts.remove(JOB);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobAfterDeadlineRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
