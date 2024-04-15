package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.transferring;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.UPDATE_JOB_EXECUTOR_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class TransferJobBetweenServersRule extends AgentScheduledRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(TransferJobBetweenServersRule.class);

	public TransferJobBetweenServersRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TRANSFER_JOB_RULE,
				"transferring job between servers",
				"transfer job from one server to another");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		return Date.from(alignStartTimeToCurrentTime(facts.get(EVENT_TIME)));
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstance = facts.get(JOB);
		return nonNull(getJobById(jobInstance.getJobId(), agentProps.getNetworkJobs()));
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final AID newServer = facts.get(AGENT);
		final JobInstanceIdentifier jobInstance = facts.get(JOB);
		final JobWithStatus jobWithStatus = ImmutableJobWithStatus.builder()
				.jobInstance(jobInstance)
				.serverName(newServer.getName())
				.changeTime(getCurrentTime())
				.build();

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Transferring job {} to server {}", jobInstance.getJobId(), newServer.getLocalName());

		agent.send(prepareJobStatusMessageForScheduler(agentProps, jobWithStatus, UPDATE_JOB_EXECUTOR_ID,
				facts.get(RULE_SET_IDX)));
		agentProps.getServerForJobMap().replace(jobInstance.getJobId(), newServer);
	}
}
