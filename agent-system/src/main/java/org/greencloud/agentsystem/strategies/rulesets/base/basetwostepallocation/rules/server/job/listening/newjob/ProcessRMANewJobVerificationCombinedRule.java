package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob.processing.ProcessRMANewJobVerificationLimitRule;
import org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob.processing.ProcessRMANewJobVerificationResourcesLackRule;
import org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob.processing.ProcessRMANewJobVerificationSuccessfulRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessRMANewJobVerificationCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessRMANewJobVerificationCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE,
				"handles new job verification",
				"rule run when Server verifies possibility of the execution of new job received from RMA");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessRMANewJobVerificationLimitRule(controller),
				new ProcessRMANewJobVerificationResourcesLackRule(controller),
				new ProcessRMANewJobVerificationSuccessfulRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMANewJobVerificationCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
