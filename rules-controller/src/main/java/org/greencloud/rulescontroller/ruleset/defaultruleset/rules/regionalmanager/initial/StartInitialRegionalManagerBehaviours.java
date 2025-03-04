package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.initial;

import static org.greencloud.commons.enums.rules.RuleType.JOB_ENERGY_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SEARCH_OWNER_AGENT_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateSubscription;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.behaviour.schedule.SchedulePeriodically;
import org.greencloud.rulescontroller.behaviour.search.SearchForAgents;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;

import jade.core.behaviours.Behaviour;

public class StartInitialRegionalManagerBehaviours extends AgentBehaviourRule<RegionalManagerAgentProps, RegionalManagerNode> {

	public StartInitialRegionalManagerBehaviours(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				InitiateSubscription.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SENSE_EVENTS_RULE, controller),
				SearchForAgents.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SEARCH_OWNER_AGENT_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, "SERVER_RESOURCE_INFORMATION_RULE", controller, true),
				ListenForMessages.create(agent, "SERVER_RESOURCE_UPDATE_RULE", controller, true),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller, true),
				ListenForMessages.create(agent, SERVER_STATUS_CHANGE_RULE, controller, true),
				ListenForMessages.create(agent, JOB_ENERGY_PRICE_RECEIVER_RULE, controller, true),
				ListenForMessages.create(agent, LISTEN_FOR_JOB_TRANSFER_RULE, controller)
		);
	}
}
