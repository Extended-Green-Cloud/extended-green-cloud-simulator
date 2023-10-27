package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.NEXT_RULE_SET_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_RULE_SET_UPDATE_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.domain.ruleset.RuleSetUpdate;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.server.ServerNode;

public class ProcessRuleSetUpdateRequestRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRuleSetUpdateRequestRule.class);

	public ProcessRuleSetUpdateRequestRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE,
				"handles rule set update messages",
				"handling messages from CNA asking Server to update its rule set");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final RuleSetUpdate updateData = facts.get(MESSAGE_CONTENT);
		logger.info("CNA asked Server to update its rule set to {}! Passing information to underlying Green Sources.",
				updateData.getRuleSetType());

		final RuleSetFacts handlerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handlerFacts.put(MESSAGE, facts.get(MESSAGE));
		handlerFacts.put(RULE_SET_TYPE, updateData.getRuleSetType());
		handlerFacts.put(NEXT_RULE_SET_TYPE, updateData.getRuleSetIdx());
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_RULE_SET_UPDATE_RULE, controller));
	}
}
