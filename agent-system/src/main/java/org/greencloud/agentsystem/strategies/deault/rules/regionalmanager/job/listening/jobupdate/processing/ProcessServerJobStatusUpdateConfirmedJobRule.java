package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.jobupdate.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_IS_PRESENT;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_DELAYED_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ACCEPTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCMA;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessServerJobStatusUpdateConfirmedJobRule
		extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerJobStatusUpdateConfirmedJobRule.class);

	public ProcessServerJobStatusUpdateConfirmedJobRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
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
		return facts.get(MESSAGE_TYPE).equals(CONFIRMED_JOB_ID) && ((boolean) facts.get(JOB_IS_PRESENT));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobWithStatus jobUpdate = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} has been confirmed as accepted in server", job.getJobId());

		agentProps.getNetworkJobs().replace(job, ACCEPTED);
		facts.put(JOB, job);

		agent.addBehaviour(ScheduleOnce.create(agent, facts, HANDLE_DELAYED_JOB_RULE, controller, SELECT_BY_FACTS_IDX));
		agent.send(prepareJobStatusMessageForCMA(agentProps, jobUpdate, ACCEPTED_JOB_ID, facts.get(RULE_SET_IDX)));
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerJobStatusUpdateConfirmedJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
