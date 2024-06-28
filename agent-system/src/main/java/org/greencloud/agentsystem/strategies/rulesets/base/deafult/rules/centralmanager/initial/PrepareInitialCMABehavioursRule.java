package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.initial;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POLL_NEXT_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;
import static org.jrba.rulesengine.constants.RuleTypeConstants.INITIALIZE_BEHAVIOURS_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateSubscription;
import org.jrba.rulesengine.behaviour.listen.ListenForMessages;
import org.jrba.rulesengine.behaviour.schedule.SchedulePeriodically;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.behaviours.Behaviour;

public class PrepareInitialCMABehavioursRule extends AgentBehaviourRule<CentralManagerAgentProps, CMANode> {

	public PrepareInitialCMABehavioursRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				InitiateSubscription.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						POLL_NEXT_JOB_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SENSE_EVENTS_RULE, controller),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller, true)
		);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(INITIALIZE_BEHAVIOURS_RULE,
				"preparing initial Central Manager behaviours",
				"when Central Manager agent is connected to RulesController, it starts initial set of behaviours");
	}

	@Override
	public AgentRule copy() {
		return new PrepareInitialCMABehavioursRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
