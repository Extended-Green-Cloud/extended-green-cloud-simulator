package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.newjob.processing.ProcessNewClientJobAlreadyExistsRule;
import org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.newjob.processing.ProcessNewClientJobQueueLimitRule;
import org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.newjob.processing.ProcessNewClientJobRule;
import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessNewClientJobCombinedRule extends AgentCombinedRule<CentralManagerAgentProps, CMANode> {

	public ProcessNewClientJobCombinedRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handling new client jobs",
				"rule run when CMA processes new Client Job message");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewClientJobAlreadyExistsRule(controller),
				new ProcessNewClientJobQueueLimitRule(controller),
				new ProcessNewClientJobRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewClientJobCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
