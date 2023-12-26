package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_DELAYED_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessServerJobStatusUpdateConfirmedJobRule
		extends AgentBasicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(ProcessServerJobStatusUpdateConfirmedJobRule.class);

	public ProcessServerJobStatusUpdateConfirmedJobRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, 6);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE,
				"handle confirmed job",
				"rule run when Server sends update regarding job confirmation");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return facts.get(MESSAGE_TYPE).equals(CONFIRMED_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Job {} has been confirmed as accepted in server", job.getJobId());

			agentProps.getNetworkJobs().replace(job, ACCEPTED);
			facts.put(JOB, job);

			agent.addBehaviour(
					ScheduleOnce.create(agent, facts, HANDLE_DELAYED_JOB_RULE, controller, SELECT_BY_FACTS_IDX));
			agent.send(prepareJobStatusMessageForScheduler(agentProps, jobStatusUpdate, CONFIRMED_JOB_ID,
					facts.get(RULE_SET_IDX)));
		}
	}
}
