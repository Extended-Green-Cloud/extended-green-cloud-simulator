package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.weatherdrop;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.EGCSRuleSetTypes.WEATHER_DROP_RULE_SET;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_TYPE;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ScheduleWeatherDropAdaptation extends AgentScheduledRule<RegionalManagerAgentProps, RMANode> {

	public ScheduleWeatherDropAdaptation(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("ADAPT_TO_WEATHER_DROP_RULE",
				"adapt the rule set upon weather drop",
				"method sends information to Servers to adapt their behaviour to the weather drop");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final Instant weatherDropTime = agentProps.getAgentKnowledge().get("WEATHER_DROP_START");
		return Date.from(weatherDropTime);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final RuleSetFacts handlerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handlerFacts.put(RULE_SET_TYPE, WEATHER_DROP_RULE_SET);
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_RULE_SET_UPDATE_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new ScheduleWeatherDropAdaptation(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
