package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.execution;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.factory.RuleSetAdaptationMessageFactory.prepareRuleSetRemovalRequest;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

public class HandleJobRemovalRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public HandleJobRemovalRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> rulesController) {
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

		if (controller.removeRuleSet(agentProps.getRuleSetForJob(), ruleSetIdx)) {
			agent.send(prepareRuleSetRemovalRequest(ruleSetIdx, agentProps.getOwnedServers().keySet()));
		}
	}
}
