package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.execution;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetRemovalRequest;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class HandleJobRemovalRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	public HandleJobRemovalRule(
			final RulesController<RegionalManagerAgentProps, RMANode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINISH_JOB_EXECUTION_RULE,
				"handle Job execution finish",
				"rule handles finish of job execution");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final int ruleSetIdx = agentProps.removeJob(job);
		agentProps.getPriceForJob().remove(job.getJobId());

		if (controller.removeRuleSet(agentProps.getRuleSetForJob(), ruleSetIdx)) {
			agent.send(prepareRuleSetRemovalRequest(ruleSetIdx, agentProps.getOwnedServers().keySet()));
		}
	}

	@Override
	public AgentRule copy() {
		return new HandleJobRemovalRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
