package org.greencloud.agentsystem.strategies.rulesets.allocation.common.validator.regionalmanager;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.VALIDATE_REGIONAL_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.VALIDATE_SERVER_ERROR_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.server.job.listening.allocation.PrepareServerPriceEstimationDataRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ValidateRegionalServersRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ValidateRegionalServersRule.class);

	public ValidateRegionalServersRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(VALIDATE_REGIONAL_ERROR_RULE,
				"validate RMA servers",
				"rule validates if there are active servers.");
	}


	@Override
	public void executeRule(final RuleSetFacts facts) {
		if (agentProps.getOwnedActiveServers().isEmpty()) {
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("There are no active servers for job execution. Sending refuse response.");

			agent.send(prepareRefuseReply(facts.get(MESSAGE)));
			facts.put(RESULT, false);
		}
		facts.put(RESULT, true);
	}

	@Override
	public AgentRule copy() {
		return new ValidateRegionalServersRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
