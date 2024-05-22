package org.greencloud.agentsystem.strategies.deault.rules.server.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.server.job.listening.newjob.processing.ProcessRMANewJobProcessingLimitRule;
import org.greencloud.agentsystem.strategies.deault.rules.server.job.listening.newjob.processing.ProcessRMANewJobSuccessfulRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessRMANewJobCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessRMANewJobCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new RMA job request",
				"handling new job sent by RMA");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessRMANewJobProcessingLimitRule(controller),
				new ProcessRMANewJobSuccessfulRule(controller)
		);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		facts.put(JOB, job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMANewJobCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
