package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.newjob.processing;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESOURCES;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NO_RESOURCES_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProcessCNANewJobNoResourcesRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessCNANewJobNoResourcesRule.class);

	public ProcessCNANewJobNoResourcesRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NO_RESOURCES_RULE,
				"handles new CNA job request - no resources",
				"handling new job sent by CNA");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Map<String, Resource> resources = facts.get(RESOURCES);
		return !areSufficient(resources, job.getRequiredResources())
				|| !agentProps.canTakeIntoProcessing()
				|| agentProps.getOwnedGreenSources().isEmpty()
				|| agentProps.isHasError();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		logger.info("Not enough available resources! Sending refuse message to Cloud Network Agent");
		agent.send(prepareRefuseReply(message));
	}
}
