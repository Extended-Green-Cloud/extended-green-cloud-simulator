package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.listening;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchContent;
import static jade.lang.acl.MessageTemplate.and;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.TRANSFER_JOB_RULE;
import static org.jrba.utils.mapper.JsonMapper.getMapper;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_TRANSFER_CONFIRMATION_TEMPLATE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSingleMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ListenForTransferConfirmationRule
		extends AgentSingleMessageListenerRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(ListenForTransferConfirmationRule.class);
	private static final long TRANSFER_EXPIRATION_TIME = 20000;

	public ListenForTransferConfirmationRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	private static String getExpectedContent(final JobInstanceIdentifier jobToTransfer) {
		try {
			return getMapper().writeValueAsString(jobToTransfer);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_CONFIRMATION_RULE,
				"listening for confirmation of job transfer",
				"rule listens for a specified period of time for Server's message confirming job transfer");
	}

	@Override
	protected MessageTemplate constructMessageTemplate(final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstance = facts.get(JOB);
		return and(LISTEN_FOR_SERVER_TRANSFER_CONFIRMATION_TEMPLATE, MatchContent(getExpectedContent(jobInstance)));
	}

	@Override
	protected long specifyExpirationTime(final RuleSetFacts facts) {
		return TRANSFER_EXPIRATION_TIME;
	}

	@Override
	protected void handleMessageProcessing(final ACLMessage message, final RuleSetFacts facts) {
		final ACLMessage serverMessage = facts.get(MESSAGE);
		final JobInstanceIdentifier jobInstance = facts.get(JOB);
		final String jobId = jobInstance.getJobId();

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

		if (message.getPerformative() == INFORM) {
			logger.info("Transfer of the job {} was confirmed. Sending information to affected server.", jobId);

			agent.send(prepareStringReply(serverMessage, TRANSFER_SUCCESSFUL_MESSAGE, INFORM));
			agent.addBehaviour(
					ScheduleOnce.create(agent, facts, TRANSFER_JOB_RULE, controller, SELECT_BY_FACTS_IDX));
		} else {
			logger.info("Transfer of the job {} has failed. Sending information to affected server.", jobId);
			agent.send(prepareStringReply(serverMessage, SERVER_INTERNAL_FAILURE_CAUSE_MESSAGE, FAILURE));
		}
	}

}
