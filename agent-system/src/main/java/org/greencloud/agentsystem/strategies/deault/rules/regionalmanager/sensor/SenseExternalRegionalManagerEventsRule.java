package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.sensor;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_DURATION;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.Optional;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.gui.event.WeatherDropEvent;
import org.jrba.environment.domain.ExternalEvent;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class SenseExternalRegionalManagerEventsRule
		extends AgentPeriodicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final long REGIONAL_MANAGER_ENVIRONMENT_SENSOR_TIMEOUT = 100;

	public SenseExternalRegionalManagerEventsRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SENSE_EVENTS_RULE,
				"sense external Regional Manager events",
				"rule listens for external events sent to the Regional Manager");
	}

	@Override
	protected long specifyPeriod() {
		return REGIONAL_MANAGER_ENVIRONMENT_SENSOR_TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return nonNull(agentNode);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final Optional<ExternalEvent> latestEvent = agentNode.getEvent();

		latestEvent.ifPresent(event -> {
			facts.put(EVENT_TIME, event.getOccurrenceTime());
			facts.put(RULE_TYPE, event.getEventType().getRuleType());
			if (event instanceof WeatherDropEvent weatherDropEvent) {
				facts.put(EVENT_DURATION, weatherDropEvent.getDuration());
			}
			controller.fire(facts);
		});
	}
}
