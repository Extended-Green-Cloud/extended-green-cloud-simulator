package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.errorhandling.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_JOB_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ProcessTransferRequestJobNotFoundRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessTransferRequestJobNotFoundRule.class);

	public ProcessTransferRequestJobNotFoundRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> rulesController) {
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
	public boolean evaluateRule(final StrategyFacts facts) {
		return ((Optional<?>) facts.get(JOB)).isEmpty();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final String jobId = facts.get(JOB_ID);

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));

		logger.info("Job {} for transfer was not found in cloud network", jobId);
		agent.send(prepareStringReply(facts.get(MESSAGE), JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
	}
}
