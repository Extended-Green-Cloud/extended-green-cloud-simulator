package org.greencloud.agentsystem.strategies.deault.rules.server.adaptation.ruleset;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetRemovalRequest;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessRMARuleSetRemovalMessageRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMARuleSetRemovalMessageRule.class);

	public ProcessRMARuleSetRemovalMessageRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE,
				"process rule set removal messages",
				"processing messages from RMA asking Server to remove given rule set");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final Integer ruleSetIdx = parseInt(message.getContent());

		MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetIdx));
		logger.info(
				"Received rule set removal request from RMA. Removing rule set with id {} and informing Green Sources",
				ruleSetIdx);

		controller.getRuleSets().remove(ruleSetIdx);
		agent.send(prepareRuleSetRemovalRequest(ruleSetIdx, agentProps.getOwnedGreenSources().keySet()));
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMARuleSetRemovalMessageRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
