package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.adaptation.ruleset;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

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
				"handling messages from RMA asking Server to remove given rule set");
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

	@Override
	public AgentRule copy() {
		return new ProcessServerRuleSetRemovalMessageRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
