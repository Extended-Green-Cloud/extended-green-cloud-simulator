package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.adaptation;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.WEATHER_DROP_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSRuleSetTypes.WEATHER_DROP_RULE_SET;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class UpdateRuleSetInCMAForWeatherDropRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(UpdateRuleSetInCMAForWeatherDropRule.class);

	public UpdateRuleSetInCMAForWeatherDropRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(WEATHER_DROP_ERROR_RULE,
				"updates rule set for weather alert event",
				"rule updates rule set to 'WeatherDrop' rule set");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Updating CMA rule set to {}!", WEATHER_DROP_RULE_SET);
		controller.addModifiedRuleSet(WEATHER_DROP_RULE_SET, controller.getLatestRuleSetIdx().get() + 1);
	}

	@Override
	public AgentRule copy() {
		return new UpdateRuleSetInCMAForWeatherDropRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
