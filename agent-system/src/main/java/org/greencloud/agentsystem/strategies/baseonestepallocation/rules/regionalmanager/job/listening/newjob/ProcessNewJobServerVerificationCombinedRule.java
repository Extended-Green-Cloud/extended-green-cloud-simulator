package org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.RESOURCES_SUFFICIENCY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;
import java.util.Map;

import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob.processing.ProcessNewJobServerVerificationNoResourcesRule;
import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob.processing.ProcessNewJobServerVerificationNotActiveRule;
import org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob.processing.ProcessNewJobServerVerificationSuccessfulRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.AID;

public class ProcessNewJobServerVerificationCombinedRule extends AgentCombinedRule<RegionalManagerAgentProps, RMANode> {

	public ProcessNewJobServerVerificationCombinedRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE,
				"handles new job verification - verifying server availability",
				"rule run when RMA verifies possibility of the execution of new job received from CMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);
		final AID server = agentProps.getServerByName(job.getServer());
		final Map<String, Resource> availableResources = agentProps.getAvailableResources(mapToClientJob(job), server);
		final boolean areResourcesSufficient = areSufficient(availableResources, job.getRequiredResources());

		facts.put(RESOURCES_SUFFICIENCY, areResourcesSufficient);
		facts.put(AGENT, server);
		return true;
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewJobServerVerificationNoResourcesRule(controller),
				new ProcessNewJobServerVerificationNotActiveRule(controller),
				new ProcessNewJobServerVerificationSuccessfulRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobServerVerificationCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
