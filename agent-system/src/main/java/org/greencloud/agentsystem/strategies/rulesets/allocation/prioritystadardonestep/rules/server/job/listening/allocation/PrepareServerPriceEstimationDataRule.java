package org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.server.job.listening.allocation;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_JOB_PRICE_ESTIMATION_RULE;
import static org.greencloud.commons.utils.facts.ValidatorFactsFactory.constructFactsForServerValidation;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PrepareServerPriceEstimationDataRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(PrepareServerPriceEstimationDataRule.class);

	public PrepareServerPriceEstimationDataRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_RULE,
				"prepares data for job allocation - evaluates job prices",
				"rule run when RMA prepares data for job allocation");
	}

	@Override
	public boolean evaluateRule(RuleSetFacts facts) {
		controller.fire(constructFactsForServerValidation(facts));
		return facts.get(RESULT);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Estimated jobs prices in response to scheduled allocation.");

		facts.put(RULE_TYPE, PREPARE_JOB_PRICE_ESTIMATION_RULE);
		controller.fire(facts);

		agent.send(prepareReply(facts.get(MESSAGE), facts.get(RESULT), facts.get(MESSAGE_TYPE)));
	}

	@Override
	public AgentRule copy() {
		return new PrepareServerPriceEstimationDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
