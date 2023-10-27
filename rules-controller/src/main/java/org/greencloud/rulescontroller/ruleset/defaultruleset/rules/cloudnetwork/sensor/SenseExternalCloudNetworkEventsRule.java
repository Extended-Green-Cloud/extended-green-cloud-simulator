package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.sensor;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT_DURATION;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static java.util.Objects.nonNull;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;
import com.gui.event.AbstractEvent;
import com.gui.event.WeatherDropEvent;

public class SenseExternalCloudNetworkEventsRule extends AgentPeriodicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final long CLOUD_NETWORK_ENVIRONMENT_SENSOR_TIMEOUT = 100;

	public SenseExternalCloudNetworkEventsRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SENSE_EVENTS_RULE,
				"sense external Cloud Network events",
				"rule listens for external events sent to the Cloud Network");
	}

	@Override
	protected long specifyPeriod() {
		return CLOUD_NETWORK_ENVIRONMENT_SENSOR_TIMEOUT;
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
