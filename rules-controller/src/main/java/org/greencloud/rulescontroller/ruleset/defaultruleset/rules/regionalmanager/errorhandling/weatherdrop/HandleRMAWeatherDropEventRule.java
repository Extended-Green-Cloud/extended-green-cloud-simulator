package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.weatherdrop;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.greencloud.commons.enums.rules.RuleSetType.WEATHER_PRE_DROP_RULE_SET;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class HandleRMAWeatherDropEventRule extends AgentBasicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(HandleRMAWeatherDropEventRule.class);

	public HandleRMAWeatherDropEventRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
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
}