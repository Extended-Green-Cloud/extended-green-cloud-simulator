package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RMA_NEW_ALLOCATED_JOBS_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForRMANewJobsAllocationRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRMANewJobsAllocationRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, AllocatedJobs.class, LISTEN_FOR_RMA_NEW_ALLOCATED_JOBS_TEMPLATE, 1,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new jobs allocation",
				"rule run when RMA sends new jobs allocation to the Server");
	}

	@Override
	public AgentRule copy() {
		return new ListenForRMANewJobsAllocationRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
