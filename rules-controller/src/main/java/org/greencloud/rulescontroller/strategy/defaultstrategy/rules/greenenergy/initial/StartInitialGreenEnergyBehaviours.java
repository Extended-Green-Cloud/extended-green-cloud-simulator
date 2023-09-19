package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.initial;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_ERROR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_RE_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_REMOVAL_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REPORT_DATA_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;

import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.behaviour.schedule.SchedulePeriodically;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

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
				SchedulePeriodically.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						REPORT_DATA_RULE, controller),
				SchedulePeriodically.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						SENSE_EVENTS_RULE, controller),
				SchedulePeriodically.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE, controller),
				new ListenForMessages(agent, LISTEN_FOR_SERVER_ERROR_RULE, controller),
				new ListenForMessages(agent, LISTEN_FOR_SERVER_RE_SUPPLY_RULE, controller),
				new ListenForMessages(agent, JOB_STATUS_RECEIVER_RULE, controller),
				new ListenForMessages(agent, NEW_JOB_RECEIVER_RULE, controller),
				new ListenForMessages(agent, LISTEN_FOR_STRATEGY_UPDATE_RULE, controller),
				new ListenForMessages(agent, LISTEN_FOR_STRATEGY_REMOVAL_RULE, controller)
		);
	}
}
