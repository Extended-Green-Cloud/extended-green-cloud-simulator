package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NO_RESOURCES_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessRefusePowerSupplyDueToErrorRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessRefusePowerSupplyDueToErrorRule.class);

	public ProcessRefusePowerSupplyDueToErrorRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NO_RESOURCES_RULE,
				"handles new Server power supply request - no resources",
				"handling new request for power supply coming from Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return agentProps.isHasError();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final EnergyJob job = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("There is not enough energy to provide for job {}. Sending refuse message.", job.getJobId());
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
