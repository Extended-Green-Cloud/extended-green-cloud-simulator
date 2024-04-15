package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.listening.processing;

import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_JOB_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessTransferRequestJobNotFoundRule extends AgentBasicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(ProcessTransferRequestJobNotFoundRule.class);

	public ProcessTransferRequestJobNotFoundRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				LISTEN_FOR_JOB_TRANSFER_HANDLE_JOB_NOT_FOUND_RULE,
				"transfer job handler - job not found",
				"handles request to transfer job from one Server to another when job was not found");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return ((Optional<?>) facts.get(JOB)).isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final String jobId = facts.get(JOB_ID);

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

		logger.info("Job {} for transfer was not found in regional manager", jobId);
		agent.send(prepareStringReply(facts.get(MESSAGE), JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
	}
}
