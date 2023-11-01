package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SCHEDULE_POWER_SUPPLY_NO_RESOURCES_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SCHEDULE_POWER_SUPPLY_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_SOURCE_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessNotEnoughResourcesBeforePowerSupplyRule
		extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessNotEnoughResourcesBeforePowerSupplyRule.class);

	public ProcessNotEnoughResourcesBeforePowerSupplyRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SCHEDULE_POWER_SUPPLY_RULE,
				PROCESS_SCHEDULE_POWER_SUPPLY_NO_RESOURCES_RULE,
				"handle accept propose from Server - no resources",
				"rule handlers Accept Proposal message to given power supply offer");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final Optional<Double> power = facts.get(RESULT);

		return power.isEmpty() || job.getEstimatedEnergy() > power.get();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final JobWithProtocol jobWithProtocol = facts.get(JOB_ID);
		final ACLMessage proposal = facts.get(MESSAGE);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Not enough power. Sending information regarding job {} failure back to server agent",
				job.getJobId());

		final RuleSetFacts finishJobFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		finishJobFacts.put(JOB, job);
		finishJobFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		controller.fire(finishJobFacts);

		final JobInstanceIdentifier jobInstanceId = jobWithProtocol.getJobInstanceIdentifier();
		final String responseProtocol = getResponseProtocol(jobWithProtocol.getReplyProtocol());

		agentProps.incrementJobCounter(jobInstanceId, FAILED);
		agent.send(prepareFailureReply(proposal, jobInstanceId, responseProtocol));
	}

	private String getResponseProtocol(final String replyProtocol) {
		return switch (replyProtocol) {
			case POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL -> FAILED_SOURCE_TRANSFER_PROTOCOL;
			case POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL -> FAILED_TRANSFER_PROTOCOL;
			default -> FAILED_JOB_PROTOCOL;
		};
	}
}
