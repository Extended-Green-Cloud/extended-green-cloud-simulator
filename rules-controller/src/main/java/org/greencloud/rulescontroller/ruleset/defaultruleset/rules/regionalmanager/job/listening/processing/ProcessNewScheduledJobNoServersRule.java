package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class ProcessNewScheduledJobNoServersRule extends AgentBasicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(ProcessNewScheduledJobNoServersRule.class);

	public ProcessNewScheduledJobNoServersRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE,
				"handles new scheduled jobs - no active Servers",
				"rule run when RMA processes new job received from RMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getOwnedActiveServers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("There are no active servers in given RMA to handle job execution! Sending refuse.");
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
