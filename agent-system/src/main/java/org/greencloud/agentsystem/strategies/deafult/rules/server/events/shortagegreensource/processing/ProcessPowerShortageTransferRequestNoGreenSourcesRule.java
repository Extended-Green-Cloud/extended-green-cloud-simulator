package org.greencloud.agentsystem.strategies.deafult.rules.server.events.shortagegreensource.processing;

import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_NO_AGENTS_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SOURCES_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForRMA;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessPowerShortageTransferRequestNoGreenSourcesRule
		extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageTransferRequestNoGreenSourcesRule.class);

	public ProcessPowerShortageTransferRequestNoGreenSourcesRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 4);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				LISTEN_FOR_JOB_TRANSFER_HANDLE_NO_AGENTS_RULE,
				"handles job transfer request when there are no other Green Sources",
				"rule handles the transfer request coming from Green Source affected by power shortage");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final AID sender = ((ACLMessage) facts.get(MESSAGE)).getSender();
		final List<AID> greenSources = agentProps.getRemainingAgents(sender, agentProps.getOwnedActiveGreenSources());

		return greenSources.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No other green sources found for job {} transfer. Changing job status.", job.getJobId());

		final String conversationId = agentProps.updateServerStateAfterFailedJobTransferBetweenGreenSources(job);

		agent.send(prepareJobStatusMessageForRMA(job, conversationId, agentProps, facts.get(RULE_SET_IDX)));
		agent.send(prepareStringReply(facts.get(MESSAGE), NO_SOURCES_AVAILABLE_CAUSE_MESSAGE, REFUSE));
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerShortageTransferRequestNoGreenSourcesRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}

