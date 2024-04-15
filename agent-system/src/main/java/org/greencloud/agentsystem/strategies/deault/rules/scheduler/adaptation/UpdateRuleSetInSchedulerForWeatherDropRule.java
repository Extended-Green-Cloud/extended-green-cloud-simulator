package org.greencloud.agentsystem.strategies.deault.rules.scheduler.adaptation;

import static org.greencloud.commons.enums.rules.EGCSRuleSetTypes.WEATHER_DROP_RULE_SET;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.WEATHER_DROP_ERROR_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;

public class UpdateRuleSetInSchedulerForWeatherDropRule
		extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(UpdateRuleSetInSchedulerForWeatherDropRule.class);

	public UpdateRuleSetInSchedulerForWeatherDropRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
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
		logger.info("Updating Scheduler rule set to {}!", WEATHER_DROP_RULE_SET);
		controller.addModifiedRuleSet(WEATHER_DROP_RULE_SET, controller.getLatestRuleSetIdx().get() + 1);
	}
}
