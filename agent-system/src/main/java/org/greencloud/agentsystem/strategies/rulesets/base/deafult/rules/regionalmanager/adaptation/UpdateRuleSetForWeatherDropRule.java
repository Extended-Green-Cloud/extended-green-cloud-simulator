package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.adaptation;

import static java.lang.Integer.parseInt;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.EGCSRuleSetTypes.WEATHER_PRE_DROP_RULE_SET;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetAdaptationRequest;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class UpdateRuleSetForWeatherDropRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(UpdateRuleSetForWeatherDropRule.class);

	public UpdateRuleSetForWeatherDropRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REQUEST_RULE_SET_UPDATE_RULE,
				"sends rule set update request",
				"rule sends to all corresponding Servers, the rule set update request");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final int nextIdx = controller.getLatestRuleSetIdx().incrementAndGet();
		return prepareRuleSetAdaptationRequest(facts.get(RULE_SET_IDX), nextIdx, facts.get(RULE_SET_TYPE),
				agentProps.getOwnedServers().keySet());
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		final String ruleSetType = facts.get(RULE_SET_TYPE);
		final int indexOfNewRuleSet = parseInt(informs.stream().findFirst().orElseThrow().getOntology());
		controller.addModifiedRuleSet(facts.get(RULE_SET_TYPE), indexOfNewRuleSet);
		logger.info("System components are changing rule set to {}!", ruleSetType);

		if (facts.get(RULE_SET_TYPE).equals(WEATHER_PRE_DROP_RULE_SET)) {
			logger.info("Scheduling adaptation for time when weather drop will start!");
			agent.addBehaviour(ScheduleOnce.create(agent, new RuleSetFacts(indexOfNewRuleSet),
					"ADAPT_TO_WEATHER_DROP_RULE", controller, SELECT_BY_FACTS_IDX));
		}
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		// case omitted
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		// case should not occur
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case should not occur
	}

	@Override
	public AgentRule copy() {
		return new UpdateRuleSetForWeatherDropRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
