package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class ProcessRMANewJobNoGreenSourcesRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMANewJobNoGreenSourcesRule.class);

	public ProcessRMANewJobNoGreenSourcesRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE,
				"handles new RMA job request - no Green Sources",
				"handling new job sent by RMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getOwnedGreenSources().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		logger.info(
				"There are no green sources connected with given Server! Sending refuse message to Regional Manager Agent");
		agent.send(prepareRefuseReply(message));
	}
}
