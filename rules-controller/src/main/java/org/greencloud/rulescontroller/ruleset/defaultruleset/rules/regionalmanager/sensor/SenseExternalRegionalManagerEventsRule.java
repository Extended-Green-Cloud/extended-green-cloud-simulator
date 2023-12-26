package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.sensor;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_DURATION;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;

import java.util.Optional;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.gui.event.AbstractEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

public class SenseExternalRegionalManagerEventsRule extends AgentPeriodicRule<RegionalManagerAgentProps, RegionalManagerNode> {

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
		final Optional<AbstractEvent> latestEvent = agentNode.getEvent();

		latestEvent.ifPresent(event -> {
			facts.put(EVENT_TIME, event.getOccurrenceTime());
			facts.put(RULE_TYPE, event.getEventTypeEnum().getRuleType());
			if (event instanceof WeatherDropEvent weatherDropEvent) {
				facts.put(EVENT_DURATION, weatherDropEvent.getDuration());
			}
			controller.fire(facts);
		});
	}
}
