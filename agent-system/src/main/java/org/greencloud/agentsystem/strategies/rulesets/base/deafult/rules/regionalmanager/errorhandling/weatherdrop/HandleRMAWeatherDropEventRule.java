package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.errorhandling.weatherdrop;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.WEATHER_DROP_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSRuleSetTypes.WEATHER_PRE_DROP_RULE_SET;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class HandleRMAWeatherDropEventRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(HandleRMAWeatherDropEventRule.class);

	public HandleRMAWeatherDropEventRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(WEATHER_DROP_ERROR_RULE,
				"handle worsening weather conditions event",
				"rule updates current system rule set");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Weather drop event detected! Updating rule set of system components!");
		agentProps.getAgentKnowledge().put("WEATHER_DROP_START", facts.get(EVENT_TIME));

		final RuleSetFacts handlerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handlerFacts.put(RULE_SET_TYPE, WEATHER_PRE_DROP_RULE_SET);
		handlerFacts.put(EVENT_TIME, facts.get(EVENT_TIME));
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_RULE_SET_UPDATE_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new HandleRMAWeatherDropEventRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
