package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.NEXT_RULE_SET_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.rulesengine.ruleset.domain.RuleSetUpdate;
import org.slf4j.Logger;

public class ProcessRuleSetUpdateRequestRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRuleSetUpdateRequestRule.class);

	public ProcessRuleSetUpdateRequestRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE,
				"handles rule set update messages",
				"handling messages from RMA asking Server to update its rule set");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final RuleSetUpdate updateData = facts.get(MESSAGE_CONTENT);
		logger.info("RMA asked Server to update its rule set to {}! Passing information to underlying Green Sources.",
				updateData.getRuleSetType());

		final RuleSetFacts handlerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handlerFacts.put(MESSAGE, facts.get(MESSAGE));
		handlerFacts.put(RULE_SET_TYPE, updateData.getRuleSetType());
		handlerFacts.put(NEXT_RULE_SET_TYPE, updateData.getRuleSetIdx());
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_RULE_SET_UPDATE_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new ProcessRuleSetUpdateRequestRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
