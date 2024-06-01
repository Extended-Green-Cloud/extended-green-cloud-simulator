package org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.initial;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ALLOCATION_DATA_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_ENERGY_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SEARCH_OWNER_AGENT_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_RESOURCE_INFORMATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_RESOURCE_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_STATUS_CHANGE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateSubscription;
import org.jrba.rulesengine.behaviour.listen.ListenForMessages;
import org.jrba.rulesengine.behaviour.schedule.SchedulePeriodically;
import org.jrba.rulesengine.behaviour.search.SearchForAgents;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.behaviours.Behaviour;

public class StartInitialRegionalManagerOneAllocationBehaviours
		extends AgentBehaviourRule<RegionalManagerAgentProps, RMANode> {

	public StartInitialRegionalManagerOneAllocationBehaviours(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

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
				ListenForMessages.create(agent, SERVER_RESOURCE_INFORMATION_RULE, controller, true),
				ListenForMessages.create(agent, SERVER_RESOURCE_UPDATE_RULE, controller, true),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller, true),
				ListenForMessages.create(agent, SERVER_STATUS_CHANGE_RULE, controller, true),
				ListenForMessages.create(agent, JOB_ENERGY_PRICE_RECEIVER_RULE, controller, true),
				ListenForMessages.create(agent, LISTEN_FOR_JOB_TRANSFER_RULE, controller),
				ListenForMessages.create(agent, ALLOCATION_DATA_REQUEST_RULE, controller, true)
		);
	}

	@Override
	public AgentRule copy() {
		return new StartInitialRegionalManagerOneAllocationBehaviours(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
