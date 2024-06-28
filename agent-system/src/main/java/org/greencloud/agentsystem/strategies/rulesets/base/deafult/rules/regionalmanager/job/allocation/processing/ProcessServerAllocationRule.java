package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.mapper.FactsMapper.mapToRuleSetFacts;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateCallForProposal;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessServerAllocationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerAllocationRule.class);

	public ProcessServerAllocationRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_NEW_JOB_ALLOCATION_RULE,
				"handle next jobs allocation",
				"handles allocation of next job batch between Servers");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);

		jobs.forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Looking for RMA for job {} execution.", job.getJobId());

			final RuleSetFacts proposalFacts = mapToRuleSetFacts(facts);
			proposalFacts.put(JOB, job);
			agent.addBehaviour(InitiateCallForProposal.create(agent, proposalFacts, LOOK_FOR_JOB_EXECUTOR_RULE,
					controller));
		});
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
