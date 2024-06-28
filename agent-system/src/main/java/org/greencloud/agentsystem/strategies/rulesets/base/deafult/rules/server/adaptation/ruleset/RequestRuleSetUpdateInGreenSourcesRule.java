package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset;

import static java.lang.Integer.parseInt;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetAdaptationRequest;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetRequestReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.NEXT_RULE_SET_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class RequestRuleSetUpdateInGreenSourcesRule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(RequestRuleSetUpdateInGreenSourcesRule.class);

	public RequestRuleSetUpdateInGreenSourcesRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REQUEST_RULE_SET_UPDATE_RULE,
				"sends rule set update request",
				"rule sends to all corresponding Green Sources, the rule set update request");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return prepareRuleSetAdaptationRequest(facts.get(RULE_SET_IDX), facts.get(NEXT_RULE_SET_TYPE),
				facts.get(RULE_SET_TYPE), agentProps.getOwnedGreenSources().keySet());
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		final String ruleSetType = facts.get(RULE_SET_TYPE);
		final int indexOfNewRuleSet = parseInt(informs.stream().findFirst().orElseThrow().getOntology());

		controller.addModifiedRuleSet(ruleSetType, indexOfNewRuleSet);
		logger.info("System components are changing rule set to {}!", ruleSetType);

		agent.send(prepareRuleSetRequestReply(facts.get(MESSAGE), facts.get(NEXT_RULE_SET_TYPE)));
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
		return new RequestRuleSetUpdateInGreenSourcesRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
