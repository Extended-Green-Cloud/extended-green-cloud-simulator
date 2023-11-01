package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ruleset;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetRequestReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.ruleset.RuleSetUpdate;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class ProcessServersRuleSetUpdateRequestRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessServersRuleSetUpdateRequestRule.class);

	public ProcessServersRuleSetUpdateRequestRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE,
				"handling rule set update messages",
				"handling messages from Server asking Green Source to update its rule set");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final RuleSetUpdate ruleSetUpdate = facts.get(MESSAGE_CONTENT);
		final int newRuleSetIdx = ruleSetUpdate.getRuleSetIdx();
		final String ruleSetType = ruleSetUpdate.getRuleSetType();

		logger.info("Server asked Green Source to update its rule set to {}! Updating rule set to index: {}.",
				ruleSetType, newRuleSetIdx);

		controller.addModifiedRuleSet(ruleSetType, newRuleSetIdx);
		agent.send(prepareRuleSetRequestReply(facts.get(MESSAGE), newRuleSetIdx));
	}
}
