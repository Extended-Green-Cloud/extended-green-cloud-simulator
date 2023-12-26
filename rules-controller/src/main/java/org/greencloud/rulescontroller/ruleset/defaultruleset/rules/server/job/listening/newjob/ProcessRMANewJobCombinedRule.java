package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RESOURCES;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.processing.ProcessRMANewJobNoGreenSourcesRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.processing.ProcessRMANewJobNoResourcesRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.processing.ProcessRMANewJobSuccessfullyRule;

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

	/**
	 * Method construct set of rules that are to be combined
	 */
	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessRMANewJobNoGreenSourcesRule(controller),
				new ProcessRMANewJobNoResourcesRule(controller),
				new ProcessRMANewJobSuccessfullyRule(controller)
		);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		final Map<String, Resource> resources = agentProps.getAvailableResources(job, null, null);

		facts.put(JOB, job);
		facts.put(RESOURCES, resources);
	}

}
