package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.initial;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_REMOVAL_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_RE_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REPORT_DATA_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForMessages;
import org.jrba.rulesengine.behaviour.schedule.SchedulePeriodically;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;

import jade.core.behaviours.Behaviour;

public class StartInitialGreenEnergyBehaviours extends AgentBehaviourRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public StartInitialGreenEnergyBehaviours(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						REPORT_DATA_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SENSE_EVENTS_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_SERVER_ERROR_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_SERVER_RE_SUPPLY_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_RULE_SET_UPDATE_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_RULE_SET_REMOVAL_RULE, controller)
		);
	}
}
