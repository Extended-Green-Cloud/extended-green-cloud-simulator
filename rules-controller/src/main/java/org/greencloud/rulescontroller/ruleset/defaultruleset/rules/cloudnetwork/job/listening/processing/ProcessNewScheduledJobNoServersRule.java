package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ProcessNewScheduledJobNoServersRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessNewScheduledJobNoServersRule.class);

	public ProcessNewScheduledJobNoServersRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE,
				"handles new scheduled jobs - no active Servers",
				"rule run when CNA processes new job received from CNA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getOwnedActiveServers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("There are no active servers in given CNA to handle job execution! Sending refuse.");
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
