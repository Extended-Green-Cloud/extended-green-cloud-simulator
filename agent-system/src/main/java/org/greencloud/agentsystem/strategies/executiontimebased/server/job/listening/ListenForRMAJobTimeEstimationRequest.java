package org.greencloud.agentsystem.strategies.executiontimebased.server.job.listening;

import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REQUEST_JOB_EXECUTION_TIME;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

import jade.lang.acl.MessageTemplate;

public class ListenForRMAJobTimeEstimationRequest extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	private static final MessageTemplate LISTEN_FOR_JOB_TIME_ESTIMATION_TEMPLATE =
			and(MatchPerformative(REQUEST), MatchProtocol(REQUEST_JOB_EXECUTION_TIME));

	public ListenForRMAJobTimeEstimationRequest(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientJob.class, LISTEN_FOR_JOB_TIME_ESTIMATION_TEMPLATE, 1,
				"JOB_EXECUTION_TIME_ESTIMATION_HANDLER_RULE");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("JOB_EXECUTION_TIME_ESTIMATION_LISTENER",
				"listen for RMA request for job execution time estimation",
				"listening for messages from RMA requesting the estimation of job execution time");
	}

	@Override
	public AgentRule copy() {
		return new ListenForRMAJobTimeEstimationRequest(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
