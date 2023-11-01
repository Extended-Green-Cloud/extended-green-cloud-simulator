package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.weatherdrop;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.greencloud.commons.enums.rules.RuleSetType.WEATHER_DROP_RULE_SET;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class HandleCNAWeatherDropEventRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(HandleCNAWeatherDropEventRule.class);

	public HandleCNAWeatherDropEventRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
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

		final RuleSetFacts handlerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handlerFacts.put(RULE_SET_TYPE, WEATHER_DROP_RULE_SET);
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_RULE_SET_UPDATE_RULE, controller));
	}
}