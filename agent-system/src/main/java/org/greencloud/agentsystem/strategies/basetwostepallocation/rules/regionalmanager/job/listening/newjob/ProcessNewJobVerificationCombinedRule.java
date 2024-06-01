package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob;

import static jade.core.Profile.AGENTS;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.processing.ProcessNewJobVerificationNoResourcesRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.processing.ProcessNewJobVerificationNoServersRule;
import org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.processing.ProcessNewJobVerificationSuccessfulRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.AID;

public class ProcessNewJobVerificationCombinedRule extends AgentCombinedRule<RegionalManagerAgentProps, RMANode> {

	public ProcessNewJobVerificationCombinedRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE,
				"handles new job verification",
				"rule run when RMA verifies possibility of the execution of new job received from CMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);
		final List<AID> servers = agentProps.selectServersForJob(mapToClientJob(job));

		facts.put(AGENTS, servers);
		return true;
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewJobVerificationSuccessfulRule(controller),
				new ProcessNewJobVerificationNoServersRule(controller),
				new ProcessNewJobVerificationNoResourcesRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobVerificationCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
