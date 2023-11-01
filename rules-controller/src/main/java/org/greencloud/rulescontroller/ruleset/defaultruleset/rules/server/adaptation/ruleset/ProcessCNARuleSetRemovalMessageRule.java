package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetRemovalRequest;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessCNARuleSetRemovalMessageRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessCNARuleSetRemovalMessageRule.class);

	public ProcessCNARuleSetRemovalMessageRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE,
				"process rule set removal messages",
				"processing messages from CNA asking Server to remove given rule set");
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
				"Received rule set removal request from CNA. Removing rule set with id {} and informing Green Sources",
				ruleSetIdx);

		controller.getRuleSets().remove(ruleSetIdx);
		agent.send(prepareRuleSetRemovalRequest(ruleSetIdx, agentProps.getOwnedGreenSources().keySet()));
	}
}
