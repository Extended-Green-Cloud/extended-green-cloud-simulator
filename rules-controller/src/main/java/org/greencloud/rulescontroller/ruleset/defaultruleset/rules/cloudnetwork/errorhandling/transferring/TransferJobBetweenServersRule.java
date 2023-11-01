package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.transferring;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class TransferJobBetweenServersRule extends AgentScheduledRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(TransferJobBetweenServersRule.class);

	public TransferJobBetweenServersRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
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

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Transferring job {} to server {}", jobInstance.getJobId(), newServer.getLocalName());

		agentProps.getServerForJobMap().replace(jobInstance.getJobId(), newServer);
	}
}
