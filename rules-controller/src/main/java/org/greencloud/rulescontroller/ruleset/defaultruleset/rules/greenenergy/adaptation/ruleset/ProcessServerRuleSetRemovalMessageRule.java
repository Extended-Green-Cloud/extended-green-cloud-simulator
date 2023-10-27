package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ruleset;

import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.lang.acl.ACLMessage;

public class ProcessServerRuleSetRemovalMessageRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessServerRuleSetRemovalMessageRule.class);

	public ProcessServerRuleSetRemovalMessageRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE,
				"handles rule set removal messages",
				"handling messages from CNA asking Server to remove given rule set");
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
		logger.info("Received rule set removal request from the Server. Removing rule set with id {}.", ruleSetIdx);
		controller.getRuleSets().remove(ruleSetIdx);
	}
}
